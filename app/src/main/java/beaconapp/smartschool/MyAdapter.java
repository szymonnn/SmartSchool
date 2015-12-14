package beaconapp.smartschool;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class MyAdapter extends ArrayAdapter<String> {

    protected Context mContext;
    protected String [] mContent;
    protected int [] mPresence;


    public MyAdapter(Context mContext, int resource, String [] content, int [] presence) {
        super(mContext, resource, content);
        this.mContext = mContext;
        this.mContent = content;
        this.mPresence = presence;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_layout, null);
            holder = new ViewHolder();
            holder.winnerIcon = (ImageView)convertView.findViewById(R.id.winnerIcon);
            holder.nameLabel = (TextView)convertView.findViewById(R.id.nameLabel);
            holder.counter = (TextView)convertView.findViewById(R.id.counter);
            holder.lp = (TextView)convertView.findViewById(R.id.lp);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder)convertView.getTag();
        }

        int [] images = {R.drawable.cart1, R.drawable.cart2, R.drawable.cart3, R.drawable.cart4, R.drawable.cart5, R.drawable.cart6, R.drawable.cart7, R.drawable.cart8, R.drawable.cart9, R.drawable.cart10, R.drawable.cart11, R.drawable.cart12};
        holder.lp.setText("" + (position+1));
        holder.nameLabel.setText(mContent[position]);
        holder.counter.setText("" + mPresence[position]);
        holder.winnerIcon.setImageResource(images[position]);
        return convertView;
    }

    private static class ViewHolder {
        ImageView winnerIcon;
        TextView nameLabel;
        TextView counter;
        TextView lp;
    }
}






