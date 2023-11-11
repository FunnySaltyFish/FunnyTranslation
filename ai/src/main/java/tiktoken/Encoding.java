package tiktoken;

import androidx.annotation.IntRange;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;

/**
 * <a href="https://github.com/mthli/tiktoken-android/blob/android/java/src/main/java/tiktoken/Encoding.java">原始代码链接</a>
 * TikToken，用于 OpenAI 系列模型
 */
@Keep
public final class Encoding implements AutoCloseable {
    static {
        System.loadLibrary("_tiktoken_jni");
    }

    @SuppressWarnings("unused")
    private long handle;

    public Encoding(@NonNull String modelName) {
        init(modelName);
    }

    public void close() {
        destroy();
    }

    private native void init(@NonNull String modelName);

    private native void destroy();

    public native long[] encode(
            @NonNull String text,
            @NonNull String[] allowedSpecialTokens,
            @IntRange(from = 0) long maxTokenLength);
}