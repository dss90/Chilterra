package cl.a2r.custom;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList; 

import cl.a2r.login.R;

public class GridViewAdapter extends BaseAdapter{
      private Context mContext;
      private ArrayList<ArrayList<String>> appsActivas = new ArrayList<ArrayList<String>>();
 
        public GridViewAdapter(Context c, ArrayList<ArrayList<String>> appsActivas) {
            mContext = c;
            this.appsActivas = appsActivas;
        }
 
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return appsActivas.size();
        }
 
        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }
 
        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }
 
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            View grid;
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (convertView == null) {
                grid = new View(mContext);
                grid = inflater.inflate(R.layout.layout_grilla, null);
                TextView textView = (TextView) grid.findViewById(R.id.grid_text);
                ImageView imageView = (ImageView)grid.findViewById(R.id.grid_image);
                textView.setText(appsActivas.get(position).get(1));
                imageView.setImageResource(Iconos.getIcon(appsActivas.get(position).get(0)));
                //layout.setDrawable();
            } else {
                grid = (View) convertView;
            }
 
            return grid;
        }
}