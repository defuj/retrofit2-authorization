package defuj.retrofit2authorization

import com.burgstaller.okhttp.AuthenticationCacheInterceptor
import com.burgstaller.okhttp.CachingAuthenticatorDecorator
import com.burgstaller.okhttp.DispatchingAuthenticator
import com.burgstaller.okhttp.digest.CachingAuthenticator
import com.burgstaller.okhttp.digest.Credentials
import com.burgstaller.okhttp.digest.DigestAuthenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.ConcurrentHashMap

class Defuj {
    companion object{
        val AUTH_TYPE_BEARER = 0
        val AUTH_TYPE_DIGEST = 1

        //for Authorization Bearer
        class OAuthInterceptor(private val tokenType: String, private val accessToken: String): Interceptor {
            override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
                var request = chain.request()
                request = request.newBuilder().header("Authorization", "$tokenType $accessToken").build()
                return chain.proceed(request)
            }
        }

        //for Authorization Digest
        fun AuthDigest(username: String, password: String): OkHttpClient {
            val builder: OkHttpClient.Builder = OkHttpClient.Builder()
            val authCache: Map<String, CachingAuthenticator> = ConcurrentHashMap()

            val credentials = Credentials(username, password)
            val digestAuthenticator = DigestAuthenticator(credentials)
            val authenticator =  DispatchingAuthenticator.Builder()
                .with("digest", digestAuthenticator).build()

            return builder
                .authenticator(CachingAuthenticatorDecorator(authenticator, authCache))
                .addInterceptor(AuthenticationCacheInterceptor(authCache))
                .build()
        }

        //for Authorization with access token : Bearer Token
        fun AuthBearer(token : String): OkHttpClient {
            return OkHttpClient.Builder()
                .addInterceptor(OAuthInterceptor("Bearer", token))
                .build()
        }

        fun Builder(url : String, client : OkHttpClient, authType : Int): Retrofit {
            return if(authType == AUTH_TYPE_BEARER){
                Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .client(client)
                    .build()
            }else{
                Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .client(client)
                    .build()
            }
        }
    }
}