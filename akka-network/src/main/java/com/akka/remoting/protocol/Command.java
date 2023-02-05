package com.akka.remoting.protocol;/*
    create qiangzhiwei time 2023/2/1
 */

import com.akka.remoting.CommandCustomHeader;
import com.akka.remoting.exception.RemotingCommandException;
import com.akka.remoting.exception.RemotingEncodeException;
import com.alibaba.fastjson.annotation.JSONField;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.akka.remoting.protocol.SerializeType.JSON;

public class Command {

    public static final String REMOTING_VERSION_KEY = "akka.remoting.version";

    protected Command() {
    }

    private static final Logger logger = LoggerFactory.getLogger(Command.class);
    private static AtomicInteger requestId = new AtomicInteger(0);

    private static final int RPC_TYPE = 0; // 0, REQUEST_COMMAND
    private static final int RPC_ONEWAY = 1; // 0, RPC


    private static volatile int configVersion = -1;

    private static final Map<Class<? extends CommandCustomHeader>, Field[]> CLASS_HASH_MAP =
            new HashMap<Class<? extends CommandCustomHeader>, Field[]>();

    private int code;
    private LanguageCode language = LanguageCode.JAVA;
    private int version = 0;
    private int opaque = requestId.getAndIncrement();
    private int flag = 0;
    private String remark;
    private HashMap<String, String> extFields;

    private transient CommandCustomHeader customHeader;


    private static final SerializeType serializeTypeConfigInThisServer = JSON;


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

    public void markOnewayRPC() {
        int bits = 1 << RPC_ONEWAY;
        this.flag |= bits;
    }

    public void markResponseType() {
        int bits = 1 << RPC_TYPE;
        this.flag |= bits;
    }

    public static Command createRequestCommand(int code, CommandCustomHeader customHeader) {
        Command cmd = new Command();
        cmd.setCode(code);
        cmd.customHeader = customHeader;
        setCmdVersion(cmd);
        return cmd;
    }

    private static void setCmdVersion(Command cmd) {
        if (configVersion >= 0) {
            cmd.setVersion(configVersion);
        } else {
            String v = System.getProperty(REMOTING_VERSION_KEY);
            if (v != null) {
                int value = Integer.parseInt(v);
                cmd.setVersion(value);
                configVersion = value;
            }
        }
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

    public static Command decode(final ByteBuf in) throws RemotingCommandException {
        final int length = in.readableBytes();
        final int oriHeaderLen = in.readInt();
        final int headerLength = getHeaderLength(oriHeaderLen);
        if (headerLength > length - 4) {
            throw new RemotingCommandException("decode error, bad header length: " + headerLength);
        }

        final Command command = headerDecode(in, headerLength, getProtocolType(oriHeaderLen));
        int bodyLength = length - 4 - headerLength;
        byte[] bodyData = null;
        if (bodyLength > 0) {
            bodyData = new byte[bodyLength];
            in.readBytes(bodyData);
        }

        if (command == null) {
            throw new RemotingCommandException("decode body error, bad header length: " + headerLength);
        }

        command.body = bodyData;
        return command;
    }

    private static Command headerDecode(ByteBuf in, int len, SerializeType type) {
        switch (type) {
            case JSON:
                byte[] headerData = new byte[len];
                in.readBytes(headerData);
                final Command resultJson = RemotingSerializable.decode(headerData, Command.class);
                resultJson.setSerializeTypeCurrentRPC(type);
                return resultJson;
            default:
                break;
        }
        return null;
    }

    private static SerializeType getProtocolType(int source) {
        return SerializeType.valueOf((byte) ((source >> 24) & 0xFF));
    }

    private static int getHeaderLength(int length) {
        return length & 0xFFFFFF;
    }

    public void fastEncodeHeader(ByteBuf out) throws RemotingEncodeException {
        final int bodySize = this.body != null ? this.body.length : 0;
        int beginIndex = out.writerIndex();

        out.writeLong(0);
        int headerSize;

        this.makeCustomHeaderToNet();
        final byte[] header = RemotingSerializable.encode(this);

        if (header == null) {
            throw new RemotingEncodeException("header encode exception" + this.toString());
        }
        headerSize = header.length;

        out.writeBytes(header);
        out.setInt(beginIndex, 4 + headerSize + bodySize);
        out.setInt(beginIndex + 4, markProtocolType(headerSize, serializeTypeCurrentRPC));
    }

    public static int markProtocolType(int source, SerializeType type) {
        return (type.getCode() << 24) | (source & 0x00FFFFFF);
    }

    public void makeCustomHeaderToNet() {
        if (this.customHeader != null) {
            Field[] fields = getClazzFields(customHeader.getClass());
            if (null == this.extFields) {
                this.extFields = new HashMap<String, String>();
            }

            for (Field field : fields) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    String name = field.getName();
                    if (!name.startsWith("this")) {
                        Object value = null;
                        try {
                            field.setAccessible(true);
                            value = field.get(this.customHeader);
                        } catch (Exception e) {
                            logger.error("Failed to access field [{}]", name, e);
                        }

                        if (value != null) {
                            this.extFields.put(name, value.toString());
                        }
                    }
                }
            }
        }
    }

    private Field[] getClazzFields(Class<? extends CommandCustomHeader> classHeader) {
        Field[] field = CLASS_HASH_MAP.get(classHeader);

        if (field == null) {
            field = classHeader.getDeclaredFields();
            synchronized (CLASS_HASH_MAP) {
                CLASS_HASH_MAP.put(classHeader, field);
            }
        }
        return field;
    }

    @Override
    public String toString() {
        return "Command{" +
                "code=" + code +
                ", language=" + language +
                ", version=" + version +
                ", opaque=" + opaque +
                ", flag=" + flag +
                ", remark='" + remark + '\'' +
                ", extFields=" + extFields +
                ", serializeTypeCurrentRPC=" + serializeTypeCurrentRPC +
                '}';
    }
}
