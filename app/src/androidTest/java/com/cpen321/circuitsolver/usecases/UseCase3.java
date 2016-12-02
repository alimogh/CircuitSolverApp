package com.cpen321.circuitsolver.usecases;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.rule.ActivityTestRule;

import com.cpen321.circuitsolver.R;
import com.cpen321.circuitsolver.ui.HomeActivity;
import com.cpen321.circuitsolver.ui.draw.DrawActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Created by Cornelis Dirk Haupt on 12/2/2016.
 */
public class UseCase3 {
    private final static String TAG = "UC3";

    private ActivityTestRule<DrawActivity> mDrawActivityRule =
            new ActivityTestRule<>(DrawActivity.class);

    @Rule
    public IntentsTestRule<HomeActivity> mHomeActivityRule =
            new IntentsTestRule<>(HomeActivity.class);

    @Before
    public void sendBitmapifNoneExists(){
        // todo: possibly populate with list of bitmaps
        if(mHomeActivityRule.getActivity().getCircuitProjects().size() == 0){
            Bitmap bm = BitmapFactory.decodeResource(
                    mHomeActivityRule.getActivity().getResources(), R.drawable.example_1);
            Util.createProjectfromBitmap(mHomeActivityRule, bm);
            mHomeActivityRule.getActivity().recreate();
        }
    }

    @Test
    public void eraseAll() {
        // - repeatedly erasing/changing values
    }

    @Test
    public void eraseMultiple() {
    }

    @Test
    public void changeAll() {
    }

    @Test
    public void changeMultiple() {
    }

    @Test
    public void autoConnectCloseComponents(){
        // - Put one component close to another -> they connect (may omit)
    }
}