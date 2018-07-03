package melonizippo.org.facerecognition;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class UncategorizedFaceView extends FrameLayout implements Checkable
{
    ImageView faceView;
    boolean isSelected;

    public UncategorizedFaceView(@NonNull Context context)
    {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.view_uncategorized_face, this);
        faceView = (ImageView) getRootView().findViewById(R.id.faceView);
    }

    public void setContent(Bitmap face)
    {
        faceView.setImageBitmap(face);
    }

    @Override
    public void setChecked(boolean checked)
    {
        isSelected = checked;

        int nextBackgroundColor;

        if(checked)
            nextBackgroundColor = Color.parseColor("#669df4");
        else
            nextBackgroundColor = Color.parseColor("#ffffff");

        View parentView = (View) faceView.getParent();
        parentView.setBackgroundColor(nextBackgroundColor);
    }

    @Override
    public boolean isChecked()
    {
        return isSelected;
    }

    @Override
    public void toggle()
    {
        setChecked(!isSelected);
    }
}
