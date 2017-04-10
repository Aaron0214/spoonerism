package filters.pre

import com.netflix.zuul.ZuulFilter
/**
 * Created by zhaojigang on 17/4/1.
 */
class FirstPreFilter extends ZuulFilter {

    @Override
    String filterType() {
        return 'pre'
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
//        println('FirstPreFilter');
    }
}
