package net.i2p.android.router.addressbook;

import java.util.List;

import net.i2p.android.router.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class AddressEntryAdapter extends ArrayAdapter<AddressEntry> {
    private final LayoutInflater mInflater;

    public AddressEntryAdapter(Context context) {
        super(context, R.layout.listitem_text);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setData(List<AddressEntry> addresses) {
        clear();
        if (addresses != null) {
            for (AddressEntry address : addresses) {
                add(address);
            }
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = mInflater.inflate(R.layout.listitem_text, parent, false);
        AddressEntry address = getItem(position);

        TextView text = (TextView) v.findViewById(R.id.text);
        text.setText(address.getHostName());

        return v;
    }
}
