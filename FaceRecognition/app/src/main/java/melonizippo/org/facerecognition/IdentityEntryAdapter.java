package melonizippo.org.facerecognition;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import melonizippo.org.facerecognition.database.IdentityEntry;

public class IdentityEntryAdapter extends ArrayAdapter<IdentityEntry>
{
    Context mContext;

    IdentityEntryAdapter(List<IdentityEntry> dataSet, Context context)
    {
        super(context, R.layout.identity_entry_view, dataSet);
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        IdentityEntry ie = getItem(position);
        if(convertView == null)
        {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.identity_entry_view, parent, false);
        }

        TextView labelView = convertView.findViewById(R.id.labelView);
        labelView.setText(ie.label);

        TextView authorizedStateView = convertView.findViewById(R.id.authorizedStateView);
        if(ie.authorized)
            authorizedStateView.setText(R.string.AuthorizedText);
        else
        {
            authorizedStateView.setText(R.string.NonAuthorizedText);
            authorizedStateView.setTextColor(Color.RED);
        }

        LinearLayout faceSamples = convertView.findViewById(R.id.faceSamples);
        FaceDataAdapter fdAdapter = new FaceDataAdapter(ie.identityDataset, getContext());
        for (int i = 0; i < fdAdapter.getCount(); i++)
        {
            faceSamples.addView(
                    fdAdapter.getView(i,null, faceSamples));
        }
        convertView.requestLayout();
        return convertView;
    }
}
