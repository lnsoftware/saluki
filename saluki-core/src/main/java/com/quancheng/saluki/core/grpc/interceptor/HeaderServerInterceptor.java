package com.quancheng.saluki.core.grpc.interceptor;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.quancheng.saluki.core.common.RpcContext;
import com.quancheng.saluki.core.common.SalukiConstants;

import io.grpc.ForwardingServerCall.SimpleForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

public class HeaderServerInterceptor implements ServerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(HeaderServerInterceptor.class);

    @Override
    public <ReqT, RespT> Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, final Metadata headers,
                                                      ServerCallHandler<ReqT, RespT> next) {
        copyMetadataToThreadLocal(headers);
        return next.startCall(new SimpleForwardingServerCall<ReqT, RespT>(call) {

            @Override
            public void sendHeaders(Metadata responseHeaders) {
                super.sendHeaders(responseHeaders);
            }
        }, headers);
    }

    private void copyMetadataToThreadLocal(Metadata headers) {
        byte[] attachmentsBytes = headers.get(SalukiConstants.GRPC_CONTEXT_ATTACHMENTS);
        byte[] valuesByte = headers.get(SalukiConstants.GRPC_CONTEXT_VALUES);
        try {
            if (attachmentsBytes != null) {
                Map<String, String> attachments = new Gson().fromJson(new String(attachmentsBytes), Map.class);
                RpcContext.getContext().setAttachments(attachments);
            }
            if (valuesByte != null) {
                Map<String, Object> values = new Gson().fromJson(new String(valuesByte), Map.class);
                for (Map.Entry<String, Object> entry : values.entrySet()) {
                    RpcContext.getContext().set(entry.getKey(), entry.getValue());
                }
            } else {
                log.info("==============================");
            }

        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }

}
