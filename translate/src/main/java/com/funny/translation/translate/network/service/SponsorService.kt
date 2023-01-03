package com.funny.translation.translate.network.service
import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.funny.translation.translate.ui.thanks.Sponsor
import retrofit2.http.GET
import retrofit2.http.Query

interface SponsorService {
    @GET("sponsor/get_all")
    suspend fun getAllSponsor() : List<Sponsor>

    @GET("sponsor/get_all_paged")
    suspend fun getPagedSponsor(
        @Query("page") page : Int = 0,
        @Query("size") size : Int = 10,
        @Query("sort") sort : String? = null
    ) : List<Sponsor>
}

class SponsorPagingSource(private val sponsorService: SponsorService, var sort: String?) : PagingSource<Int, Sponsor>() {
    companion object {
        private const val TAG = "SponsorPagingSource"
    }

    override fun getRefreshKey(state: PagingState<Int, Sponsor>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Sponsor> {
        return try {
            val nextPage = params.key ?: 0
            val data = sponsorService.getPagedSponsor(nextPage, sort = sort)
            Log.d(TAG, "load: $nextPage ${data.size}")
            LoadResult.Page(
                data = data,
                prevKey = if (nextPage == 0) null else nextPage - 1,
                nextKey = if (data.isNotEmpty()) nextPage + 1 else null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoadResult.Error(e)
        }
    }
}