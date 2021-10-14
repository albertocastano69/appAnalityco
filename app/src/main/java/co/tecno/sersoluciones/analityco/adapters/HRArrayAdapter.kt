package co.tecno.sersoluciones.analityco.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import java.text.Normalizer
import java.util.*

@Suppress("unused")
class HRArrayAdapter<T> : BaseAdapter, Filterable {
    /**
     * Contains the list of objects that represent the data of this ArrayAdapter.
     * The content of this list is referred to as "the array" in the documentation.
     */
    private var mObjects: MutableList<T>? = null

    /**
     * Lock used to modify the content of [.mObjects]. Any write operation
     * performed on the array should be synchronized on this lock. This lock is also
     * used by the filter (see [.getFilter] to make a synchronized copy of
     * the original array of data.
     */
    private val mLock = Any()

    /**
     * The resource indicating what views to inflate to display the content of this
     * array adapter.
     */
    private var mResource = 0

    /**
     * The resource indicating what views to inflate to display the content of this
     * array adapter in a drop down widget.
     */
    private var mDropDownResource = 0

    /**
     * If the inflated resource is not a TextView, [.mFieldId] is used to find
     * a TextView inside the inflated views hierarchy. This field must contain the
     * identifier that matches the one defined in the resource file.
     */
    private var mFieldId = 0

    /**
     * Indicates whether or not [.notifyDataSetChanged] must be called whenever
     * [.mObjects] is modified.
     */
    private var mNotifyOnChange = true

    /**
     * Returns the context associated with this array adapter. The context is used
     * to create views from the resource passed to the constructor.
     *
     * @return The Context associated with this adapter.
     */
    var context: Context? = null
        private set
    private var mOriginalValues: ArrayList<T>? = null
    private var mFilter: HRArrayFilter? = null
    private var mInflater: LayoutInflater? = null

    /**
     * Constructor
     *
     * @param context            The current context.
     * @param textViewResourceId The resource ID for a layout file containing a TextView to use when
     * instantiating views.
     */
    constructor(context: Context, textViewResourceId: Int) {
        init(context, textViewResourceId, 0, ArrayList())
    }

    /**
     * Constructor
     *
     * @param context            The current context.
     * @param resource           The resource ID for a layout file containing a layout to use when
     * instantiating views.
     * @param textViewResourceId The id of the TextView within the layout resource to be populated
     */
    constructor(context: Context, resource: Int, textViewResourceId: Int) {
        init(context, resource, textViewResourceId, ArrayList())
    }

    /**
     * Constructor
     *
     * @param context            The current context.
     * @param textViewResourceId The resource ID for a layout file containing a TextView to use when
     * instantiating views.
     * @param objects            The objects to represent in the ListView.
     */
    constructor(context: Context, textViewResourceId: Int, objects: Array<T>) {
        init(context, textViewResourceId, 0, Arrays.asList(*objects))
    }

    /**
     * Constructor
     *
     * @param context            The current context.
     * @param resource           The resource ID for a layout file containing a layout to use when
     * instantiating views.
     * @param textViewResourceId The id of the TextView within the layout resource to be populated
     * @param objects            The objects to represent in the ListView.
     */
    constructor(context: Context, resource: Int, textViewResourceId: Int, objects: Array<T>) {
        init(context, resource, textViewResourceId, Arrays.asList(*objects))
    }

    /**
     * Constructor
     *
     * @param context            The current context.
     * @param textViewResourceId The resource ID for a layout file containing a TextView to use when
     * instantiating views.
     * @param objects            The objects to represent in the ListView.
     */
    constructor(context: Context, textViewResourceId: Int, objects: MutableList<T>) {
        init(context, textViewResourceId, 0, objects)
    }

    /**
     * Constructor
     *
     * @param context            The current context.
     * @param resource           The resource ID for a layout file containing a layout to use when
     * instantiating views.
     * @param textViewResourceId The id of the TextView within the layout resource to be populated
     * @param objects            The objects to represent in the ListView.
     */
    constructor(context: Context, resource: Int, textViewResourceId: Int, objects: MutableList<T>) {
        init(context, resource, textViewResourceId, objects)
    }

    /**
     * Adds the specified object at the end of the array.
     *
     * @param object The object to add at the end of the array.
     */
    fun add(`object`: T) {
        if (mOriginalValues != null) {
            synchronized(mLock) {
                mOriginalValues!!.add(`object`)
                if (mNotifyOnChange) notifyDataSetChanged()
            }
        } else {
            mObjects!!.add(`object`)
            if (mNotifyOnChange) notifyDataSetChanged()
        }
    }

    /**
     * Inserts the specified object at the specified index in the array.
     *
     * @param object The object to insert into the array.
     * @param index  The index at which the object must be inserted.
     */
    fun insert(`object`: T, index: Int) {
        if (mOriginalValues != null) {
            synchronized(mLock) {
                mOriginalValues!!.add(index, `object`)
                if (mNotifyOnChange) notifyDataSetChanged()
            }
        } else {
            mObjects!!.add(index, `object`)
            if (mNotifyOnChange) notifyDataSetChanged()
        }
    }

    /**
     * Removes the specified object from the array.
     *
     * @param object The object to remove.
     */
    fun remove(`object`: T) {
        if (mOriginalValues != null) {
            synchronized(mLock) { mOriginalValues!!.remove(`object`) }
        } else {
            mObjects!!.remove(`object`)
        }
        if (mNotifyOnChange) notifyDataSetChanged()
    }

    /**
     * Remove all elements from the list.
     */
    fun clear() {
        if (mOriginalValues != null) {
            synchronized(mLock) { mOriginalValues!!.clear() }
        } else {
            mObjects!!.clear()
        }
        if (mNotifyOnChange) notifyDataSetChanged()
    }

    /**
     * Sorts the content of this adapter using the specified comparator.
     *
     * @param comparator The comparator used to sort the objects contained
     * in this adapter.
     */
    fun sort(comparator: Comparator<in T>?) {
        Collections.sort(mObjects!!, comparator)
        if (mNotifyOnChange) notifyDataSetChanged()
    }

    /**
     * {@inheritDoc}
     */
    override fun notifyDataSetChanged() {
        super.notifyDataSetChanged()
        mNotifyOnChange = true
    }

    /**
     * Control whether methods that change the list ([.add],
     * [.insert], [.remove], [.clear]) automatically call
     * [.notifyDataSetChanged].  If set to false, caller must
     * manually call notifyDataSetChanged() to have the changes
     * reflected in the attached view.
     *
     *
     * The default is true, and calling notifyDataSetChanged()
     * resets the flag to true.
     *
     * @param notifyOnChange if true, modifications to the list will
     * automatically call [                       ][.notifyDataSetChanged]
     */
    fun setNotifyOnChange(notifyOnChange: Boolean) {
        mNotifyOnChange = notifyOnChange
    }

    private fun init(context: Context, resource: Int, textViewResourceId: Int, objects: MutableList<T>) {
        this.context = context
        mInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mDropDownResource = resource
        mResource = mDropDownResource
        mObjects = objects
        mFieldId = textViewResourceId
    }

    /**
     * {@inheritDoc}
     */
    override fun getCount(): Int {
        return mObjects!!.size
    }

    /**
     * {@inheritDoc}
     */
    override fun getItem(position: Int): T {
        return mObjects!![position]
    }

    /**
     * Returns the position of the specified item in the array.
     *
     * @param item The item to retrieve the position of.
     * @return The position of the specified item.
     */
    fun getPosition(item: T): Int {
        return mObjects!!.indexOf(item)
    }

    /**
     * {@inheritDoc}
     */
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    /**
     * {@inheritDoc}
     */
    override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
        return createViewFromResource(position, convertView, parent, mResource)
    }

    private fun createViewFromResource(position: Int, convertView: View?, parent: ViewGroup,
                                       resource: Int): View {
        val view: View
        val text: TextView
        view = convertView ?: mInflater!!.inflate(resource, parent, false)
        text = try {
            if (mFieldId == 0) {
                //  If no custom field is assigned, assume the whole resource is a TextView
                view as TextView
            } else {
                //  Otherwise, find the TextView field within the layout
                view.findViewById<View>(mFieldId) as TextView
            }
        } catch (e: ClassCastException) {
            Log.e("ArrayAdapter", "You must supply a resource ID for a TextView")
            throw IllegalStateException(
                    "ArrayAdapter requires the resource ID to be a TextView", e)
        }
        text.text = getItem(position).toString()
        return view
    }

    /**
     *
     * Sets the layout resource to create the drop down views.
     *
     * @param resource the layout resource defining the drop down views
     * @see .getDropDownView
     */
    fun setDropDownViewResource(resource: Int) {
        mDropDownResource = resource
    }

    /**
     * {@inheritDoc}
     */
    override fun getDropDownView(position: Int, convertView: View, parent: ViewGroup): View {
        return createViewFromResource(position, convertView, parent, mDropDownResource)
    }

    /**
     * {@inheritDoc}
     */
    override fun getFilter(): HRArrayFilter {
        if (mFilter == null) {
            mFilter = HRArrayFilter()
        }
        return mFilter!!
    }

    /**
     *
     * An array filter constrains the content of the array adapter with
     * a prefix. Each item that does not start with the supplied prefix
     * is removed from the list.
     */
    inner class HRArrayFilter : Filter() {
        override fun performFiltering(prefix: CharSequence): FilterResults {
            val results = FilterResults()
            if (mOriginalValues == null) {
                synchronized(mLock) { mOriginalValues = ArrayList(mObjects!!) }
            }
            if (prefix.isEmpty()) {
                synchronized(mLock) {
                    val list = ArrayList(mOriginalValues!!)
                    results.values = list
                    results.count = list.size
                }
            } else {
                val prefixString = prefix.toString().toLowerCase(Locale.getDefault())
                val values = mOriginalValues
                val count = values!!.size
                val newValues = ArrayList<T>(count)
//                val noPalatals = ArrayList<String>()
                for (i in 0 until count) {
                    val value = values[i]
                    val valueText = value.toString().toLowerCase(Locale.getDefault())
                    val valueTextNoPalatals = Normalizer
                            .normalize(valueText, Normalizer.Form.NFD)
                            .replace("[^\\p{ASCII}]".toRegex(), "")
                            .toLowerCase(Locale.getDefault())
                    val prefixStringNoPalatals = Normalizer
                            .normalize(prefixString, Normalizer.Form.NFD)
                            .replace("[^\\p{ASCII}]".toRegex(), "")
                            .toLowerCase(Locale.getDefault())
//                    val valueTextNoPalatals = toNoPalatals (valueText);
//                    val prefixStringNoPalatals = toNoPalatals (prefixString);

                    //Log.d( "DATA NORMAL", valueText + ", " + prefixString );
                    //Log.d( "DATA NO PALATALS", valueTextNoPalatals + ", " + prefixStringNoPalatals );

                    // First match against the whole, non-splitted value
                    if (valueText.startsWith(prefixString) || valueTextNoPalatals.startsWith(prefixStringNoPalatals)) {
                        newValues.add(value)
                    } else {
                        val words = valueText.split(" ".toRegex()).toTypedArray()
                        val wordCount = words.size
                        for (k in 0 until wordCount) {
                            if (words[k].startsWith(prefixString)) {
                                newValues.add(value)
                                break
                            }
                        }
                    }
                }
                results.values = newValues
                results.count = newValues.size
            }
            return results
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            mObjects = results.values as MutableList<T>
            if (results.count > 0) {
                notifyDataSetChanged()
            } else {
                notifyDataSetInvalidated()
            }
        }

        private fun toNoPalatals(original: String): String {
            var original = original
            original = original.replace("Č", "C")
            original = original.replace("Ć", "C")
            original = original.replace("Š ", "S")
            original = original.replace("Đ", "D")
            original = original.replace("Ž", "Z")
            original = original.replace("č", "c")
            original = original.replace("ć", "c")
            original = original.replace("š", "s")
            original = original.replace("đ", "d")
            original = original.replace("ž", "z")
            return original
        }
    }

    companion object {
        /**
         * Creates a new ArrayAdapter from external resources. The content of the array is
         * obtained through [android.content.res.Resources.getTextArray].
         *
         * @param context        The application's environment.
         * @param textArrayResId The identifier of the array to use as the data source.
         * @param textViewResId  The identifier of the layout used to create views.
         * @return An ArrayAdapter<CharSequence>.
        </CharSequence> */
        fun createFromResource(context: Context,
                               textArrayResId: Int, textViewResId: Int): HRArrayAdapter<CharSequence> {
            val strings = context.resources.getTextArray(textArrayResId)
            return HRArrayAdapter(context, textViewResId, strings)
        }
    }
}