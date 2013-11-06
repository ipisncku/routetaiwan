package tw.ipis.routetaiwan;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.graphics.Paint;
import android.os.SystemClock;
import android.view.View;

public class GifView extends View {
	private long movieStart;
	private Movie movie;
	//此处必须重写该构造方法
	public GifView(Context context) {
		super(context);
		//以文件流（InputStream）读取进gif图片资源
		movie=Movie.decodeStream(getResources().openRawResource(R.drawable.btn_info));
	}

	@Override
	protected void onDraw(Canvas canvas) {
	    canvas.drawColor(Color.TRANSPARENT);
	    super.onDraw(canvas);
	    long now = android.os.SystemClock.uptimeMillis();
	    if (movieStart == 0) {
	        movieStart = now;
	    }
	    if (movie != null) {
	        int relTime = (int) ((now - movieStart) % movie.duration());
	        movie.setTime(relTime);
	        movie.draw(canvas, getWidth() - movie.width(), getHeight() - movie.height());
	        this.invalidate();
	    }
	}
}