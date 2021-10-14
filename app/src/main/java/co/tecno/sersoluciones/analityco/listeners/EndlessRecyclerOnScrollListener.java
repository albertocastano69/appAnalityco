package co.tecno.sersoluciones.analityco.listeners;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

public abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {

    private boolean loading = false; // True if we are still waiting for the last set of data to load.
    private final int visibleThreshold = 2; // The minimum amount of items to have below your current scroll position before loading more.
    private int firstVisibleItem;
    private int visibleItemCount;
    private int totalItemCount;

    private int current_page = 1;

    private final LinearLayoutManager mLinearLayoutManager;

    public EndlessRecyclerOnScrollListener(LinearLayoutManager linearLayoutManager) {
        this.mLinearLayoutManager = linearLayoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        if (dy < 0) {
            return;
        }
        // check for scroll down only
        visibleItemCount = recyclerView.getChildCount();
        totalItemCount = mLinearLayoutManager.getItemCount();
        firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();

        // to make sure only one onLoadMore is triggered
        synchronized (this) {
            if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                // End has been reached, Do something
                current_page++;
                onLoadMore();
                loading = true;
            }
        }
    }


    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    protected abstract void onLoadMore();

}