package sample.app

import org.koin.dsl.module

object SampleKoinModule {
    val module = module{
        single {
            "SampleKoinModule is initialized"
        }
    }
}