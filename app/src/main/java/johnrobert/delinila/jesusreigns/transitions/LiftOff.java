package johnrobert.delinila.jesusreigns.transitions;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.transition.Transition;
import android.transition.TransitionValues;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import johnrobert.delinila.jesusreigns.R;


/**
 * A transition that animates the elevation of a View from a given value down to zero.
 * <p>
 * Useful for creating parent↔child navigation transitions
 * (https://www.google.com/design/spec/patterns/navigational-transitions.html#navigational-transitions-parent-to-child)
 * when combined with a {@link android.transition.ChangeBounds} on a shared element.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class LiftOff extends Transition {

    private static final String PROPNAME_ELEVATION = "plaid:liftoff:elevation";

    private static final String[] transitionProperties = {
            PROPNAME_ELEVATION
    };

    private final float initialElevation;
    private final float finalElevation;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LiftOff(Context context, AttributeSet attrs) {
        super(context, attrs);
        final TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.LiftOff);
        initialElevation = ta.getDimension(R.styleable.LiftOff_initialElevation, 0f);
        finalElevation = ta.getDimension(R.styleable.LiftOff_finalElevation, 0f);
        ta.recycle();
    }

    @Override
    public String[] getTransitionProperties() {
        return transitionProperties;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void captureStartValues(TransitionValues transitionValues) {
        transitionValues.values.put(PROPNAME_ELEVATION, initialElevation);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void captureEndValues(TransitionValues transitionValues) {
        transitionValues.values.put(PROPNAME_ELEVATION, finalElevation);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues,
                                   TransitionValues endValues) {
        return ObjectAnimator.ofFloat(endValues.view, View.TRANSLATION_Z,
                initialElevation, finalElevation);
    }

}
