package http.view;

import com.github.jknack.handlebars.Template;
import http.HttpRequest;
import http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemplateView implements View {
    private static final Logger logger = LoggerFactory.getLogger(TemplateView.class);
    private Template template;

    public TemplateView(Template template) {
        this.template = template;
    }

    @Override
    public void render(HttpRequest request, HttpResponse response) throws Exception {
        logger.debug("attributes : {}", request.getAttributes());
        byte[] body = template.apply(request.getAttributes()).getBytes();
        response.response200Header(body.length);
        response.responseBody(body);
    }
}
