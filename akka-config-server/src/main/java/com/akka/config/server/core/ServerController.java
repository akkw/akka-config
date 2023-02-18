package com.akka.config.server.core;/*
    create qiangzhiwei time 2023/2/9
 */

import com.akka.config.ha.controller.HaController;
import com.akka.config.ha.etcd.EtcdClient;
import com.akka.config.ha.etcd.EtcdConfig;
import com.akka.config.handler.CommandHandler;
import com.akka.config.protocol.CommandCode;
import com.akka.config.protocol.Response;
import com.akka.config.protocol.ResponseCode;
import com.akka.config.server.handler.ActivateCommandHandler;
import com.akka.config.server.handler.ActivateMultiCommandHandler;
import com.akka.config.server.handler.CreateCommandHandler;
import com.akka.config.server.handler.CreateNamespaceHandler;
import com.akka.config.server.handler.DeleteCommandHandler;
import com.akka.config.server.handler.MetadataCommandHandler;
import com.akka.config.server.handler.ReadAllCommandHandler;
import com.akka.config.server.handler.ReadCommandHandler;
import com.akka.config.server.handler.VerifyCommandHandler;
import com.akka.config.server.handler.VerifyMultiCommandHandler;
import com.akka.config.server.protocol.MetadataEvent;
import com.akka.config.store.Store;
import com.akka.config.store.mysql.MysqlStore;
import com.akka.remoting.netty.NettyRequestProcessor;
import com.akka.remoting.netty.NettyServerConfig;
import com.akka.remoting.protocol.Command;
import com.akka.tools.api.LifeCycle;
import com.akka.tools.bus.AsyncEventBus;
import com.akka.tools.bus.Event;
import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.CompletableFuture;

public class ServerController implements LifeCycle {

    private final static Logger logger = LoggerFactory.getLogger(ServerController.class);

    private final EtcdDataListener etcdDataListener;

    private final AsyncEventBus<MetadataEvent> metadataBus = new AsyncEventBus<>();

    private final EtcdClient etcdClient;

    private final HaController haController;

    private final Timer leaderTimer;

    private final EtcdConfig etcdConfig;

    private final Store configStore;

    private final ServerNetwork serverNetwork;

    private final Map<CommandCode, CommandHandler> requestHandlerMap = new HashMap<>();

    private final MetadataManager metadataManager = new MetadataManager();

    public ServerController(EtcdClient etcdClient) {
        this.etcdClient = etcdClient;
        this.etcdConfig = etcdClient.getConfig();
        this.leaderTimer = new Timer("CheckNamespaceLeaderThread");
        this.etcdDataListener = new EtcdDataListener(etcdClient, metadataBus);
        this.haController = new HaController(etcdClient, etcdConfig.getPathConfig());
        this.configStore = new MysqlStore();
        this.serverNetwork = new ServerNetwork(new NettyServerConfig());
    }

    @Override
    public void start() {
        this.metadataBus.addStation(this::metadataHandler);
//        this.etcdDataListener.start();

        final NettyRequestProcessor processor = new NettyRequestProcessor() {
            @Override
            public Command processRequest(ChannelHandlerContext ctx, Command command) throws Exception {
                final CommandCode requestCode = CommandCode.valueOf(command.getCode());
                final CommandHandler handler = ServerController.this.requestHandlerMap.get(requestCode);
                if (handler != null) {
                    CompletableFuture<Response> future = null;
                    try {
                        future = handler.commandHandler(command);
                    } catch (Exception e) {
                        writeResponse(null, e, command, ctx);
                        return null;
                    }
                    future.whenCompleteAsync((c, t) -> {
                        writeResponse(c, t, command, ctx);
                    });
                }
                return null;
            }

            @Override
            public boolean rejectRequest() {
                return false;
            }
        };
        this.serverNetwork.registerProcessor(CommandCode.READ, processor);
        this.serverNetwork.registerProcessor(CommandCode.CREATE, processor);
        this.serverNetwork.registerProcessor(CommandCode.DELETE, processor);
        this.serverNetwork.registerProcessor(CommandCode.METADATA, processor);
        this.serverNetwork.registerProcessor(CommandCode.ACTIVATE, processor);
        this.serverNetwork.registerProcessor(CommandCode.VERIFY, processor);
        this.serverNetwork.registerProcessor(CommandCode.CREATE_NAMESPACE, processor);
        this.serverNetwork.registerProcessor(CommandCode.ACTIVATE_MULTI, processor);
        this.serverNetwork.registerProcessor(CommandCode.VERIFY_MULTI_CONFIG, processor);
        this.serverNetwork.registerProcessor(CommandCode.READ_ALL_CONFIG, processor);
        this.serverNetwork.start();
        this.etcdClient.start();
        initHandler();
    }

    private void writeResponse(Response response, Throwable t, Command reqCommand, ChannelHandlerContext ctx) {
        Command respCommand = null;
        try {
            if (t != null) {
                //the t should not be null, using error code instead
                throw t;
            } else {
                respCommand = handleResponse(response, reqCommand);
                respCommand.markResponseType();
                ctx.writeAndFlush(respCommand);
            }
        } catch (Throwable e) {
            logger.error("Process request over, but fire response failed, request:[{}] response:[{}]", reqCommand, response, e);
        }
    }
    public Command handleResponse(Response response, Command command) {
        Command remotingCommand = Command.createResponseCommand(ResponseCode.SUCCESS.code(), null);
        remotingCommand.setBody(JSON.toJSONBytes(response));
        remotingCommand.setOpaque(command.getOpaque());
        return remotingCommand;
    }

    private void initHandler() {
        requestHandlerMap.put(CommandCode.CREATE, new CreateCommandHandler(this.configStore, metadataManager));
        requestHandlerMap.put(CommandCode.DELETE, new DeleteCommandHandler(this.configStore, metadataManager));
        requestHandlerMap.put(CommandCode.READ, new ReadCommandHandler(this.configStore));
        requestHandlerMap.put(CommandCode.METADATA, new MetadataCommandHandler(etcdClient, metadataManager));
        requestHandlerMap.put(CommandCode.ACTIVATE, new ActivateCommandHandler(this.configStore, metadataManager));
        requestHandlerMap.put(CommandCode.VERIFY, new VerifyCommandHandler(this.configStore, metadataManager));
        requestHandlerMap.put(CommandCode.CREATE_NAMESPACE, new CreateNamespaceHandler(this.etcdClient));
        requestHandlerMap.put(CommandCode.ACTIVATE_MULTI, new ActivateMultiCommandHandler(this.etcdClient));
        requestHandlerMap.put(CommandCode.VERIFY_MULTI_CONFIG, new VerifyMultiCommandHandler(this.etcdClient));
        requestHandlerMap.put(CommandCode.READ_ALL_CONFIG, new ReadAllCommandHandler(this.etcdClient));
    }


    @Override
    public void stop() {

    }


    private void metadataHandler(Event<MetadataEvent> metadataEventEvent) {

        final MetadataEvent metadataEvent = metadataEventEvent.getLoad();
        final String namespace = metadataEvent.getNamespace();
        final String environment = metadataEvent.getEnvironment();
        boolean electionResult = haController.election(namespace);

        metadataManager.createOrUpdateMetadata(namespace, environment, metadataEvent.getMetadata());
        if (electionResult) {
            logger.info("the [{}] successfully elected the leader.", namespace);
        } else {
            logger.warn("the [{}] failed to elect the leader.", namespace);
        }
    }
}
