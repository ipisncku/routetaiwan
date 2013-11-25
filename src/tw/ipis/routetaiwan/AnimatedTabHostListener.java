package tw.ipis.routetaiwan;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

/**
 * A custom OnTabChangeListener that uses the TabHost its related to to fetch information about the current and previous
 * tabs. It uses this information to perform some custom animations that slide the tabs in and out from left and right.
 * 
 * @author Daniel Kvist
 * 
 */
public class AnimatedTabHostListener implements OnTabChangeListener
{

	private static final int ANIMATION_TIME = 300;
	private TabHost tabHost;
	private View previousView;
	private View currentView;
	private int currentTab;
	private Context parent;

	/**
	 * Constructor that takes the TabHost as a parameter and sets previousView to the currentView at instantiation
	 * 
	 * @param context
	 * @param tabHost
	 */
	public AnimatedTabHostListener(Context context, TabHost tabHost)
	{
		this.tabHost = tabHost;
		this.previousView = tabHost.getCurrentView();
		parent = context;
	}

	/**
	 * When tabs change we fetch the current view that we are animating to and animate it and the previous view in the
	 * appropriate directions.
	 */
	@Override
	public void onTabChanged(String tabId)
	{

		currentView = tabHost.getCurrentView();
		if (tabHost.getCurrentTab() > currentTab)
		{
			previousView.setAnimation(outToLeftAnimation());
			currentView.setAnimation(inFromRightAnimation());
		}
		else
		{
			previousView.setAnimation(outToRightAnimation());
			currentView.setAnimation(inFromLeftAnimation());
		}
		previousView = currentView;
		currentTab = tabHost.getCurrentTab();

	}

	/**
	 * Custom animation that animates in from right
	 * 
	 * @return Animation the Animation object
	 */
	private Animation inFromRightAnimation()
	{
		Animation inFromRight = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT,
				0.0f);
		return setProperties(inFromRight);
	}

	/**
	 * Custom animation that animates out to the right
	 * 
	 * @return Animation the Animation object
	 */
	private Animation outToRightAnimation()
	{
		Animation outToRight = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT,
				1.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
		return setProperties(outToRight);
	}

	/**
	 * Custom animation that animates in from left
	 * 
	 * @return Animation the Animation object
	 */
	private Animation inFromLeftAnimation()
	{
		Animation inFromLeft = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, -1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT,
				0.0f);
		return setProperties(inFromLeft);
	}

	/**
	 * Custom animation that animates out to the left
	 * 
	 * @return Animation the Animation object
	 */
	private Animation outToLeftAnimation()
	{
		Animation outtoLeft = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT,
				-1.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
		return setProperties(outtoLeft);
	}

	/**
	 * Helper method that sets some common properties
	 * 
	 * @param animation
	 *            the animation to give common properties
	 * @return the animation with common properties
	 */
	private Animation setProperties(Animation animation)
	{
		animation.setDuration(ANIMATION_TIME);
		animation.setInterpolator(AnimationUtils.loadInterpolator(parent,  
                android.R.anim.decelerate_interpolator));
		return animation;
	}
}