package melonizippo.org.facerecognition;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import melonizippo.org.facerecognition.database.FaceData;

public class FaceDataAdapter extends ArrayAdapter<FaceData>
{
    private List<FaceData> dataSet;
    Context mContext;

    public FaceDataAdapter(List<FaceData> data, Context context)
    {
        super(context, R.layout.face_preview, data);
        this.dataSet = data;
        this.mContext=context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        FaceData fd = getItem(position);
        if (convertView == null)
        {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.face_preview, parent, false);
            ImageView imageView = (ImageView) convertView.findViewById(R.id.facePreview);
            imageView.setImageBitmap(fd.toBitmap());
        }

        return convertView;
    }
}
