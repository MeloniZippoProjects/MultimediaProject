package melonizippo.org.facerecognition;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.List;

import melonizippo.org.facerecognition.database.FaceData;

public class FaceDataAdapter extends ArrayAdapter<FaceData>
{
    Context mContext;

    FaceDataAdapter(List<FaceData> dataSet, Context context)
    {
        super(context, R.layout.face_preview, dataSet);
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
        }

        ImageView imageView = convertView.findViewById(R.id.facePreview);
        imageView.setImageBitmap(fd.toBitmap());

        return convertView;
    }
}
