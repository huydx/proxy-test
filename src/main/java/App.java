import java.util.concurrent.CompletableFuture;

import com.linecorp.armeria.client.ClientBuilder;
import com.linecorp.armeria.client.ClientFactory;
import com.linecorp.armeria.client.ClientFactoryBuilder;
import com.linecorp.armeria.client.HttpClient;
import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.HttpRequestDuplicator;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.SessionProtocol;
import com.linecorp.armeria.common.util.EventLoopGroups;
import com.linecorp.armeria.server.HttpService;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.ServiceRequestContext;

public class App {
    public static void main(String[] args) {
        ClientFactory clientFactory = new ClientFactoryBuilder()
                .workerGroup(EventLoopGroups.newEventLoopGroup(1), true)
                .build();
        ClientBuilder clientBuilder = new ClientBuilder("none+http://127.0.0.1:9998").factory(clientFactory);
        final HttpClient httpClient = clientBuilder.build(HttpClient.class);

        ServerBuilder sb =  new ServerBuilder().port(20080, SessionProtocol.HTTP);
        sb.service("/", new HttpService() {
            @Override
            public HttpResponse serve(ServiceRequestContext ctx, HttpRequest req) throws Exception {
                HttpRequestDuplicator duplicator = new HttpRequestDuplicator(req);
                HttpRequest dupReq = duplicator.duplicateStream();
                CompletableFuture<HttpResponse> res = dupReq.aggregate().thenApply( r -> {
                    return httpClient.execute(r.toHttpRequest());
                });
                return HttpResponse.from(res);
            }
        });
        sb.build().start().join();
    }
}
