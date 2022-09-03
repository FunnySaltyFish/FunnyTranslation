package com.funny.translation.translate.database

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//class TransHistoryPagingSource(private val transHistoryDao: TransHistoryDao) : PagingSource<Int, TransHistoryBean>() {
//    companion object {
//        private const val TAG = "TransHisPagingSource"
//    }
//
//    override fun getRefreshKey(state: PagingState<Int, TransHistoryBean>): Int? = null
//
//    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, TransHistoryBean> = withContext(Dispatchers.IO){
//         try {
//            val nextPage = params.key ?: 0
//            val data = transHistoryDao.queryPaged(nextPage)
//            Log.d(TAG, "load: $nextPage ${data.size}")
//            LoadResult.Page(
//                data = data,
//                prevKey = if (nextPage == 0) null else nextPage - 1,
//                nextKey = if (data.isNotEmpty()) nextPage + 1 else null
//            )
//        } catch (e: Exception) {
//            e.printStackTrace()
//            LoadResult.Error(e)
//        }
//    }
//}