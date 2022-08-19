package ru.playsoftware.j2meloader.applist;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ru.playsoftware.j2meloader.R;
import app.dao.AppItem;

public class AppsListAdapter extends BaseAdapter implements Filterable {

	private List<AppItem> list = new ArrayList<>();
	private List<AppItem> filteredList = new ArrayList<>();
	private final AppFilter appFilter = new AppFilter();
	private CharSequence filterConstraint;

	@Override
	public int getCount() {
		return filteredList.size();
	}

	@Override
	public AppItem getItem(int position) {
		return filteredList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup viewGroup) {
		ViewHolder holder;
		if (view == null) {
			LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
			view = layoutInflater.inflate(R.layout.list_row_jar, viewGroup, false);
			holder = new ViewHolder(view);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		AppItem item = filteredList.get(position);
		Drawable icon = Drawable.createFromPath(item.getImagePathExt());
		if (icon != null) {
			icon.setFilterBitmap(false);
			holder.icon.setImageDrawable(icon);
		} else {
			holder.icon.setImageResource(R.mipmap.ic_launcher);
		}
		holder.name.setText(item.getTitle());
		holder.author.setText(item.getAuthor());
		holder.version.setText(item.getVersion());

		return view;
	}

	public void setItems(List<AppItem> items) {
		list = items;
		appFilter.filter(filterConstraint);
	}

	@Override
	public Filter getFilter() {
		return appFilter;
	}

	private static class ViewHolder {
		ImageView icon;
		TextView name;
		TextView author;
		TextView version;

		private ViewHolder(View rootView) {
			icon = rootView.findViewById(R.id.list_image);
			name = rootView.findViewById(R.id.list_title);
			author = rootView.findViewById(R.id.list_author);
			version = rootView.findViewById(R.id.list_version);
		}
	}

	private class AppFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			if (TextUtils.isEmpty(constraint)) {
				results.count = list.size();
				results.values = list;
			} else {
				ArrayList<AppItem> resultList = new ArrayList<>();
				for (AppItem item : list) {
					if (item.getTitle().toLowerCase().contains(constraint)
							|| item.getAuthor().toLowerCase().contains(constraint)) {
						resultList.add(item);
					}
				}
				results.count = resultList.size();
				results.values = resultList;
			}
			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			filterConstraint = constraint;
			if (results.values != null) {
				//noinspection unchecked
				filteredList = (List<AppItem>) results.values;
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}
	}
}
