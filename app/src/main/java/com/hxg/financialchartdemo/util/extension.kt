import android.app.Activity
import android.app.Dialog
import android.content.res.TypedArray
import android.support.annotation.IdRes
import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.hxg.financialchartdemo.BuildConfig

//inflate helper
fun Activity.Inflate(@LayoutRes layoutResId: Int, parent: ViewGroup? = null): View {
    return layoutInflater.inflate(layoutResId, parent, false)
}

//inflate helper
fun Activity.InflateAndAdd(@LayoutRes layoutResId: Int, parent: ViewGroup): View {
    return layoutInflater.inflate(layoutResId, parent, true)
}

//get the root layout group of this activity.
fun Activity.RootLayout(): ViewGroup? {
    return bindOptional<ViewGroup>(android.R.id.content)?.getChildAt(0) as? ViewGroup
}

//findView helpers
fun <T : View> Activity.bind(@IdRes res: Int): T {
    return findViewById(res) ?: throw Exception("bind: required view not found in activity layout")
}

fun <T : View> Activity.bindOptional(@IdRes res: Int): T? {
    return findViewById(res)
}

fun <T : View> android.support.v4.app.Fragment.bind(@IdRes res: Int): T {
    return view?.findViewById(res)
            ?: throw Exception("bind: required view not found in frament layout")
}

fun <T : View> android.support.v4.app.Fragment.bindOptional(@IdRes res: Int): T? {
    return view?.findViewById(res)
}

fun <T : View> Dialog.bind(@IdRes res: Int): T {
    return findViewById(res) ?: throw Exception("bind: required view not found in dialog layout")
}

fun <T : View> Dialog.bindOptional(@IdRes res: Int): T? {
    return findViewById(res)
}

fun <T : View> RecyclerView.ViewHolder.bind(@IdRes res: Int): T {
    return itemView.findViewById(res)
            ?: throw Exception("bind: required view not found in viewholder layout")
}

fun <T : View> RecyclerView.ViewHolder.bindOptional(@IdRes res: Int): T? {
    return itemView.findViewById(res)
}

fun <T : View> View.bind(@IdRes res: Int): T {
    return findViewById(res) ?: throw Exception("bind: required view not found in frament layout")
}

fun <T : View> View.bindOptional(@IdRes res: Int): T? {
    return findViewById(res)
}

fun <T> TypedArray.Use(b: (TypedArray) -> T): T {
    try {
        return b(this)
    } finally {
        this.recycle()
    }
}

fun View.RemoveClick() {
    this.setOnClickListener(null)
}

fun View.OnClick(c: View.OnClickListener) {
    this.setOnClickListener(c)
}

inline fun SafeNoLog(protected: () -> Unit) {
    try {
        protected()
    } catch (e: Throwable) {
        if (BuildConfig.DEBUG) {
            e.printStackTrace()
        }
    }
}

//quick way to write try/catch all error
inline fun Safe(protected: () -> Unit) {
    try {
        protected()
    } catch (e: Throwable) {
        e.printStackTrace()
    }
}

fun isNullOrEmpty(list: List<Any>?): Boolean {
    return list == null || list.isEmpty()
}

