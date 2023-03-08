package com.akka.config.handler;/* 
    create qiangzhiwei time 2023/2/10
 */

import com.akka.config.protocol.Metadata;
import com.akka.config.protocol.Response;
import com.akka.config.protocol.ResponseCode;
import com.akka.remoting.protocol.Command;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface CommandHandler {

    default void fillResponse(Response response, ResponseCode responseCode) {
        response.setCode(responseCode.code());
        response.setMessage(responseCode.getDesc().getBytes(StandardCharsets.UTF_8));
    }

    default void fillClientVersion(Set<Metadata.ClientVersion> metadataVersionSet, List<Metadata.ClientVersion> requestVersionList) {
        if (requestVersionList == null || requestVersionList.isEmpty()) {
            if (metadataVersionSet != null && !metadataVersionSet.isEmpty()) {
                metadataVersionSet.clear();
            }
            return;
        }
        final Map<String, Metadata.ClientVersion> clientVersionMap = metadataVersionSet.
                stream().collect(Collectors.toMap(Metadata.ClientVersion::getClient, Function.identity()));

        for (Metadata.ClientVersion client : requestVersionList) {
            final Metadata.ClientVersion clientVersion = clientVersionMap.get(client.getClient());
            if (clientVersion != null) {
                clientVersion.setVersion(client.getVersion());
            } else {
                metadataVersionSet.add(client);
            }
        }
        metadataVersionSet.removeIf(x -> !requestVersionList.contains(x));
//        requestVersionList.forEach(x -> metadataVersionSet.removeIf((y) -> !x.getClient().equals(y.getClient())));
    }

    CompletableFuture<Response> commandHandler(Command command) throws ExecutionException, InterruptedException, SQLException, NoSuchFieldException, InstantiationException, IllegalAccessException;
}
