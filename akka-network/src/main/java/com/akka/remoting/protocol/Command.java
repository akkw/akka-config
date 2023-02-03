package com.akka.remoting.protocol;/*
    create qiangzhiwei time 2023/2/1
 */

import com.akka.remoting.CommandCustomHeader;
import com.akka.remoting.exception.RemotingCommandException;
import com.alibaba.fastjson.annotation.JSONField;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Command {
    private static AtomicInteger requestId = new AtomicInteger(0);

    private static final int RPC_TYPE = 0; // 0, REQUEST_COMMAND
    private static final int RPC_ONEWAY = 1; // 0, RPC


    private static volatile int configVersion = -1;


    private int code;
    private LanguageCode language = LanguageCode.JAVA;
    private int version = 0;
    private int opaque = requestId.getAndIncrement();
    private int flag = 0;
    private String remark;
    private HashMap<String, String> extFields;

    private transient CommandCustomHeader customHeader;


    private static final SerializeType serializeTypeConfigInThisServer = SerializeType.JSON;


    private SerializeType serializeTypeCurrentRPC = serializeTypeConfigInThisServer;

    private transient byte[] body;


    @JSONField(serialize = false)
    public boolean isOnewayRPC() {
        int bits = 1 << RPC_ONEWAY;
        return (this.flag & bits) == bits;
    }

    @JSONField(serialize = false)
    public RemotingCommandType getType() {
        if (this.isResponseType()) {
            return RemotingCommandType.RESPONSE_COMMAND;
        }

        return RemotingCommandType.REQUEST_COMMAND;
    }

    public void markResponseType() {
        int bits = 1 << RPC_TYPE;
        this.flag |= bits;
    }
    public static Command createResponseCommand(int code, String remark) {
        return createResponseCommand(code, remark, null);
    }

    public static Command createResponseCommand(int code, String remark,
                                                        Class<? extends CommandCustomHeader> classHeader) {
        Command cmd = new Command();
        cmd.markResponseType();
        cmd.setCode(code);
        cmd.setRemark(remark);
        cmd.setVersion(configVersion);

        if (classHeader != null) {
            try {
                CommandCustomHeader objectHeader = classHeader.getDeclaredConstructor().newInstance();
                cmd.customHeader = objectHeader;
            } catch (InstantiationException e) {
                return null;
            } catch (IllegalAccessException e) {
                return null;
            } catch (InvocationTargetException e) {
                return null;
            } catch (NoSuchMethodException e) {
                return null;
            }
        }

        return cmd;
    }


    @JSONField(serialize = false)
    public boolean isResponseType() {
        int bits = 1 << RPC_TYPE;
        return (this.flag & bits) == bits;
    }

    public static AtomicInteger getRequestId() {
        return requestId;
    }

    public int getCode() {
        return code;
    }

    public LanguageCode getLanguage() {
        return language;
    }

    public int getVersion() {
        return version;
    }

    public int getOpaque() {
        return opaque;
    }

    public int getFlag() {
        return flag;
    }

    public String getRemark() {
        return remark;
    }

    public HashMap<String, String> getExtFields() {
        return extFields;
    }

    public static SerializeType getSerializeTypeConfigInThisServer() {
        return serializeTypeConfigInThisServer;
    }

    public SerializeType getSerializeTypeCurrentRPC() {
        return serializeTypeCurrentRPC;
    }

    public byte[] getBody() {
        return body;
    }

    public static void setRequestId(AtomicInteger requestId) {
        Command.requestId = requestId;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setLanguage(LanguageCode language) {
        this.language = language;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setOpaque(int opaque) {
        this.opaque = opaque;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setExtFields(HashMap<String, String> extFields) {
        this.extFields = extFields;
    }

    public void setSerializeTypeCurrentRPC(SerializeType serializeTypeCurrentRPC) {
        this.serializeTypeCurrentRPC = serializeTypeCurrentRPC;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public ByteBuffer encode() {


        return null;
    }


    public static Command decode(final byte[] array) throws RemotingCommandException {

        return null;
    }

    public static Command decode(final ByteBuffer byteBuffer) throws RemotingCommandException {
        return decode(Unpooled.wrappedBuffer(byteBuffer));
    }

    public static Command decode(final ByteBuf byteBuffer) throws RemotingCommandException {


        return null;
    }
}
