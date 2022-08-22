package app.profile

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.utils.onClick
import app.utils.onLongClick

class ProfilesAdapter(val list: MutableList<Profile>) : RecyclerView.Adapter<ProfilesAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: TextView) : RecyclerView.ViewHolder(itemView){

        fun bind(item: Profile){

            (itemView as TextView).text = if(isDefault(item)) "${item.name} (default)" else item.name

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ProfileItemView(parent.context)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])

        holder.itemView.onClick {
            control?.onClickItem(position, it)
        }

    }

    override fun getItemCount() = list.size

    fun getItem(index: Int) = list[index]

    @SuppressLint("NotifyDataSetChanged")
    fun addItem(profile: Profile) {
        if (!list.contains(profile)) {
            list.add(profile)
            //list.sort()
            notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun removeItem(position: Int) {
        list.removeAt(position)
        notifyDataSetChanged()
    }

    private var def: Profile? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setDefault(profile: Profile) {
        def = profile
        notifyDataSetChanged()
    }

    fun isDefault(profile: Profile) = def == profile

    var control: Control? = null

    interface Control {

        fun onClickItem(position: Int, view: View)

    }

}