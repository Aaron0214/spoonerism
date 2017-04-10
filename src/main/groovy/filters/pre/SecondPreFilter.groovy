package filters.pre

import com.netflix.zuul.ZuulFilter
/**
 * Created by zhaojigang on 17/4/1.
 */
class SecondPreFilter extends ZuulFilter{

    @Override
    String filterType() {
        return 'pre'
    }

    @Override
    int filterOrder() {
        return 101
    }

    @Override
    boolean shouldFilter() {
        return true
    }

    @Override
    Object run() {
//        println('SecondPreFilter')
    }
}
