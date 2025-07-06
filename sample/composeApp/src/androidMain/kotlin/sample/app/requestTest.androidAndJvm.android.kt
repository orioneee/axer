package sample.app

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import okhttp3.Interceptor
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ContextGetter() : KoinComponent {
    fun getContext(): Context {
        val context: Context by inject()
        return context
    }
}

actual fun getIntercepors(): List<Interceptor> {
    return listOf(ChuckerInterceptor(ContextGetter().getContext()))
}