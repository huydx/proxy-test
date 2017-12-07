import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import com.linecorp.armeria.client.ClientBuilder;
import com.linecorp.armeria.client.ClientFactory;
import com.linecorp.armeria.client.ClientFactoryBuilder;
import com.linecorp.armeria.client.Clients;
import com.linecorp.armeria.client.HttpClient;
import com.linecorp.armeria.common.AggregatedHttpMessage;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.SessionProtocol;
import com.linecorp.armeria.common.util.EventLoopGroups;
import com.linecorp.armeria.common.util.SafeCloseable;
import com.linecorp.armeria.server.HttpService;
import com.linecorp.armeria.server.ServerBuilder;

public class App {
    public static void main(String[] args) {
        ClientFactory clientFactory = new ClientFactoryBuilder()
                .workerGroup(EventLoopGroups.newEventLoopGroup(100), true)
                .build();
        ClientBuilder clientBuilder = new ClientBuilder("none+http://127.0.0.1:9998").factory(clientFactory);
        final HttpClient httpClient = clientBuilder.build(HttpClient.class);
        AtomicInteger count = new AtomicInteger(0);

        ServerBuilder sb =  new ServerBuilder().port(20080, SessionProtocol.HTTP).defaultRequestTimeoutMillis(0);
        sb.service("/test", (HttpService) (ctx, req) -> {
            int c = count.incrementAndGet();
            System.out.println(c);
            return httpClient.execute(req);
        });
        sb.build().start().join();
    }
}
