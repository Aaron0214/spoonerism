package filters.route

import com.aaron.spoonerism.client.AsyncHttpClientImpl
import com.aaron.spoonerism.service.RuleHelper
import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.Debug
import com.netflix.zuul.context.RequestContext
import com.netflix.zuul.util.HTTPRequestUtils
import com.ning.http.client.AsyncHttpClient
import com.ning.http.client.ListenableFuture
import com.ning.http.client.Param
import com.ning.http.client.Request
import com.ning.http.client.Response
import org.apache.commons.lang.StringUtils

import javax.servlet.http.HttpServletRequest

/**
 * Created by zhaojigang on 17/4/1.
 */
class FirstRouteFilter extends ZuulFilter {

    public static final String CONTENT_ENCODING = "Content-Encoding";

    @Override
    String filterType() {
        return 'route'
    }

    @Override
    int filterOrder() {
        return 100
    }

    @Override
    boolean shouldFilter() {
        return true
    }

    @Override
    Object run() {
        AsyncHttpClientImpl asyncHttpClient = new AsyncHttpClientImpl();
        try {
            Request request = buildRequest(asyncHttpClient);
            if (null == request) {
                return;
            }
            ListenableFuture<Response> listenableFuture = asyncHttpClient.executeRequest(request);
            Response response = listenableFuture.get();
            setResponse(response);
            return response;
        } catch (Exception e) {
            throw e;
        } finally {
            asyncHttpClient?.close();
        }
    }


    static Request buildRequest(AsyncHttpClientImpl asyncHttpClient) {
        HttpServletRequest servletRequest = RequestContext.getCurrentContext().getRequest();
        String url = buildQueryUrl(servletRequest);
        if (StringUtils.isBlank(url)) {
            return;
        }
        url += getQueryString(servletRequest);
        String method = servletRequest.getMethod();
        AsyncHttpClient.BoundRequestBuilder builder = asyncHttpClient.requestBuilder(method, url);
        Request request = builder.build();
        addBody(request, servletRequest);
        addHeader(request, servletRequest);
        return request;
    }

    static String buildQueryUrl(HttpServletRequest request) {
        String url = request.getRequestURL().toString();
        String newUrl = RuleHelper.getRuleUrl(url);
        if (StringUtils.isBlank(newUrl)) {
            url = url.replace(request.getRequestURI(), "");
            newUrl = RuleHelper.getRuleUrl(url) + request.getRequestURI();
        }
        return newUrl;
    }

    static String getQueryString(HttpServletRequest request) {
        String encoding = "UTF-8"
        String currentQueryString = request.getQueryString()
        if (StringUtils.isBlank(currentQueryString)) {
            return ""
        }

        String rebuiltQueryString = ""
        for (String keyPair : currentQueryString.split("&")) {
            if (rebuiltQueryString.length() > 0) {
                rebuiltQueryString = rebuiltQueryString + "&"
            }

            if (keyPair.contains("=")) {
                def (name, value) = keyPair.split("=", 2)
                value = URLDecoder.decode(value, encoding)
                value = new URI(null, null, null, value, null).toString().substring(1)
                value = value.replaceAll('&', '%26')
                rebuiltQueryString = rebuiltQueryString + name + "=" + value
            } else {
                def value = URLDecoder.decode(keyPair, encoding)
                value = new URI(null, null, null, value, null).toString().substring(1)
                rebuiltQueryString = rebuiltQueryString + value
            }
        }
        return "?" + rebuiltQueryString
    }

    static void addHeader(Request request, HttpServletRequest servletRequest) {
        Enumeration<String> headerNames = servletRequest.getHeaderNames()
        while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            if (header != "host") {
                if (request.headers.containsKey(header)) {
                    request.headers.remove(header);
                }
                request.headers.add(header, servletRequest.getHeader(header));
            }
        }
    }

    static void addBody(Request request, HttpServletRequest servletRequest) {
        String method = servletRequest.getMethod();
        switch (method.toLowerCase()) {
            case "post":
                request.streamData = servletRequest.getInputStream();
                List<Param> paramList = new ArrayList<>();
                while (servletRequest.getParameterNames().hasMoreElements()) {
                    String name = servletRequest.getParameterNames().nextElement();
                    servletRequest.getParameterMap().get(name)[0]
                    Param param = new Param(name, servletRequest.getParameterMap().get(name)[0]);
                    if (paramList.contains(param)) {
                        Integer index = paramList.indexOf(param);
                        paramList.remove(index);
                        paramList.add(index, param);
                    } else {
                        paramList.add(param);
                    }
                }
                request.formParams = paramList;
                break;
            default:
                return;
        }
    }

    void setResponse(Response response) {
        Debug.addRequestDebug("ZUUL :: ${response.getUri()}")
        Debug.addRequestDebug("ZUUL :: Response statusLine > ${response.getStatusText()}")
        Debug.addRequestDebug("ZUUL :: Response code > ${response.getStatusCode()}")
        RequestContext context = RequestContext.getCurrentContext()

        context.set("hostZuulResponse", response)
        context.setResponseStatusCode(response.getStatusCode())
        context.responseDataStream = response?.getResponseBodyAsStream()

        boolean isOriginResponseGzipped = false

        for (String h : response.getHeaders(CONTENT_ENCODING)) {
            if (HTTPRequestUtils.getInstance().isGzipped(h)) {
                isOriginResponseGzipped = true;
                break;
            }
        }
        context.setResponseGZipped(isOriginResponseGzipped);


        if (Debug.debugRequest()) {
            if (context.responseDataStream) {
                byte[] origBytes = context.getResponseDataStream().bytes
                context.setResponseDataStream(new ByteArrayInputStream(origBytes))
            }
        } else {
            for (String header in response.getHeaders().keySet()) {
                context.addOriginResponseHeader(header, response.getHeader(header))

                if (header.equalsIgnoreCase("content-length"))
                    context.setOriginContentLength(response.getHeader(header))

                if (isValidHeader(header)) {
                    context.addZuulResponseHeader(header, response.getHeader(header));
                }
            }
        }

    }

    boolean isValidHeader(String header) {
        switch (header.toLowerCase()) {
            case "connection":
            case "content-length":
            case "content-encoding":
            case "server":
            case "transfer-encoding":
                return false
            default:
                return true
        }
    }
}
