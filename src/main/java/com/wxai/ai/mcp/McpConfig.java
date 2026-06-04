package com.wxai.ai.mcp;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
public class McpConfig {

    @Value("${bigmodel.api-key}")
    private String apiKey;

    @Value("${bigmodel.mcp.pool-size:3}")
    private int poolSize;

    @Bean
    public McpToolProvider mcpToolProvider(){

        List<McpClient> clients = new ArrayList<>();
        for (int i = 0; i < poolSize; i++) {
            HttpMcpTransport transport = new HttpMcpTransport.Builder()
                    .sseUrl("https://open.bigmodel.cn/api/mcp/web_search/sse?Authorization=" + apiKey)
                    .logRequests(false)
                    .logResponses(false)
                    .build();

            McpClient client = new DefaultMcpClient.Builder()
                    .key("mcp-client-" + i)
                    .transport(transport)
                    .build();
            clients.add(client);
        }
        log.info("MCP 连接池初始化完成，数量: {}", poolSize);

        return McpToolProvider.builder()
                .mcpClients(clients)
                .build();
    }
}