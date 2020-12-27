package com.ls.libcommon.extention;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

public abstract class AbsPagedListAdapter<T,VH extends RecyclerView.ViewHolder> extends PagedListAdapter<T,VH> {

    //保存头部和尾部
    private SparseArray<View> mHeaders = new SparseArray<>();
    private SparseArray<View> mFooters = new SparseArray<>();

    private int BASE_ITEM_TYPE_HEADER = 100000;
    private int BASE_ITEM_TYPE_FOOTER = 200000;

    protected AbsPagedListAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback) {
        super(diffCallback);
    }

    public void addHeaderView(View view){
        if (mHeaders.indexOfValue(view) < 0){
            //不包含view
            mHeaders.put(BASE_ITEM_TYPE_HEADER++,view);
            notifyDataSetChanged();
        }
    }

    public void addFooterView(View view){
        if (mFooters.indexOfValue(view) < 0){
            //不包含view
            mHeaders.put(BASE_ITEM_TYPE_FOOTER++,view);
            notifyDataSetChanged();
        }
    }

    /**
     * 添加头部尾部之后，ItemCount就要包含头部和尾部了
     * @return
     */
    @Override
    public int getItemCount() {
        int itemCount = super.getItemCount();
        return mHeaders.size() + mFooters.size() + itemCount;
    }

    /**
     * 获取原始adapter的itemCount
     * @return
     */
    public int getOriginalItemCount(){
        return getItemCount() - mHeaders.size() - mFooters.size();
    }

    @Override
    public int getItemViewType(int position) {
        //把Header和Footer拦截下来，其他item交给子类处理
        if (isHeaderPosition(position)){
            //返回该position对应的headerView的 viewType
            return mHeaders.keyAt(position);
        }

        if (isFooterView(position)){
            //Footer的position，需要减去Header和原始adapter的itemCount
            position = position - getOriginalItemCount()-mHeaders.size();
            return mFooters.keyAt(position);
        }

        //正常的position也需要把Header减掉
        position = position - mHeaders.size();
        return getOriginalItemViewType(position);
    }

    protected abstract int getOriginalItemViewType(int position);

    private boolean isFooterView(int position){
        //position大于原始adapter的itemCount和mHeader的总长度
        return position >= getOriginalItemCount() + mHeaders.size();
    }

    private boolean isHeaderPosition(int position){
        //position小于Header的大小
        return position<mHeaders.size();
    }

    /**
     * 根据View的type去返回一个
     * @param parent
     * @param viewType 就是上面getItemViewType()方法返回的type
     * @return
     */
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mHeaders.indexOfKey(viewType) >= 0){
            //在Header里面找到了view
            View view = mHeaders.get(viewType);
            return (VH) new androidx.recyclerview.widget.RecyclerView.ViewHolder(view){};
        }

        if (mFooters.indexOfKey(viewType) >= 0){
            //在Footer里面找到了view
            View view = mFooters.get(viewType);
            return (VH) new androidx.recyclerview.widget.RecyclerView.ViewHolder(view){};
        }

        //正常的View交给子类去处理
        return onCreateOriginalViewHolder(parent,viewType);
    }

    protected abstract VH onCreateOriginalViewHolder(ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        //把Header和Footer的View拦截下来
        if (isHeaderPosition(position)||isFooterView(position))
            return;

        //其他类型的View交给子类去实现
        position = position - mHeaders.size();
        onBindOriginalViewHolder(holder,position);
    }

    protected abstract void onBindOriginalViewHolder(VH holder, int position);

    /**
     * 问题：
     * 如果我们在请求网络数据成功之前，就把Headers加到RecyclerView里面去了，那么数据返回后就不会添加到Headers之后的列表上面。
     * 因为我们请求成功的PagedList里面没有HeaderView数据，就会导致Paging在计算ItemView个数的时候有问题，后面就会出现位置错乱
     *
     *
     * 解决：
     * paging在页面刷新之后，都会执行到recyclerView的AdapterDataObserver里面的onChanged()...方法中去。其中，有错误的其实是这个方法的positionStart参数
     *  @Override
     *  public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
     *      assertNotInLayoutOrScroll(null);
     *      if (mAdapterHelper.onItemRangeChanged(positionStart, itemCount, payload)) {
     *          triggerUpdateProcessor();
     *      }
     *  }
     *  这里的positionStart是没有包含我们HeaderView的数量，所以我们只要把这个positionStart + Header.size()就可以了
     *
     *  因为这个Observer是在我们给RecyclerView的setAdapter()方法中注册的
     *
     *   public void setAdapter(@Nullable Adapter adapter) {
     *       //...
     *       setAdapterInternal(adapter, false, true);
     *       //...
     *   }
     *
     *  private void setAdapterInternal(@Nullable Adapter adapter, boolean compatibleWithPrevious,
     *             boolean removeAndRecycleViews) {
     *      //...
     *      if (adapter != null) {
     *          adapter.registerAdapterDataObserver(mObserver);
     *          adapter.onAttachedToRecyclerView(this);
     *      }
     *      //...
     *  }
     *
     *  所以，我们只要在observer中计算出正确的值，再传给adapter就可以了
     * @param observer
     */
    @Override
    public void registerAdapterDataObserver(@NonNull RecyclerView.AdapterDataObserver observer) {
        super.registerAdapterDataObserver(new AdapterDataObserverProxy(observer));
    }

    /**
     * 给AdapterDataObserver设置代理
     */
    private class AdapterDataObserverProxy extends RecyclerView.AdapterDataObserver{

        //直接代理这个对象
        private RecyclerView.AdapterDataObserver mObserver;

        public AdapterDataObserverProxy(RecyclerView.AdapterDataObserver observer) {
            mObserver = observer;
        }

        @Override
        public void onChanged() {
            mObserver.onChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            mObserver.onItemRangeChanged(positionStart+mHeaders.size(), itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
            mObserver.onItemRangeChanged(positionStart+mHeaders.size(), itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            mObserver.onItemRangeInserted(positionStart+mHeaders.size(), itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            mObserver.onItemRangeRemoved(positionStart+mHeaders.size(), itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            mObserver.onItemRangeMoved(fromPosition+mHeaders.size(), toPosition, itemCount);
        }
    }
}
