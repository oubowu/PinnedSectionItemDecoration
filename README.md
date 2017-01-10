# Pinned section header decoration for RecyclerView.([Please pay attention to a better project StickyItemDecoration](https://github.com/oubowu/StickyItemDecoration))「[中文版文档点这里](https://github.com/oubowu/PinnedSectionItemDecoration/blob/master/README-CN.md)」
A powerful pinned section header library. The realization of ideas comes from「[pinned-section-item-decoration](https://github.com/takahr/pinned-section-item-decoration)」. Please mark a star if you think it's helpful.（＾∀＾）
##  Function  
- Large pinned section header support vertical orientation of the LinearLayoutManager, GridLayoutManager and StaggeredGridLayoutManager.
- Small pinned section header support vertical orientation of the LinearLayoutManager and GridLayoutManager which its span count is 1.
- Support the header of the click, double click and long press event.
- Support the child view of the click, double click and long press event.
- It can draw the separator line and support custom separator line style. (PS: Vertical staggeredgrid layout requires fixed height, can not be randomly changed to lead to item position switch, refer to「[See line 89-108 of MainActivity](https://github.com/oubowu/PinnedSectionItemDecoration/blob/master/app%2Fsrc%2Fmain%2Fjava%2Fcom%2Foushangfeng%2Fpinneddemo%2FMainActivity.java#L89-L108)」)

## Screenshot
![大标签线性布局](/pic/big_header_linearlayout.gif) 
![大标签网格布局](/pic/big_header_gridlayout.gif) 
![大标签瀑布流布局](/pic/big_header_staggeredgridlayout.gif) 
![小标签线性布局](/pic/small_header_linearlayout.gif) 
![股市Demo](/pic/stock_demo.gif) 

## Extension library
[BaseRecyclerViewAdapterHelper](https://github.com/CymChad/BaseRecyclerViewAdapterHelper)(It is highly recommended to use this adapter, which can greatly reduce the amount of work. The current demo is using v2.1.0.)

## How to use?

To add a dependency using Gradle:
```groovy
compile 'com.oushangfeng:PinnedSectionItemDecoration:1.2.4'
```

Adapter needs to process the span count of header through the FullSpanUtil.
```
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        FullSpanUtil.onAttachedToRecyclerView(recyclerView, this, StockEntity.StockInfo.TYPE_HEADER);
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        FullSpanUtil.onViewAttachedToWindow(holder, this, StockEntity.StockInfo.TYPE_HEADER);
    }
```

To achieve large pinned section header, RecyclerView only need to add a PinnedHeaderItemDecoration. Note that the top layer of the header where the layout can not be set marginTop.「[See StockActivity](https://github.com/oubowu/PinnedSectionItemDecoration/blob/master/app%2Fsrc%2Fmain%2Fjava%2Fcom%2Foushangfeng%2Fpinneddemo%2FStockActivity.java#L53-L83)」
``` 
      OnHeaderClickAdapter clickAdapter = new OnHeaderClickAdapter() {

          @Override
          public void onHeaderClick(View view, int id, int position) {
              switch (id) {
                  case R.id.fl:
                       // case OnItemTouchListener.HEADER_ID:
                       Toast.makeText(StockActivity.this, "click, tag: " + mAdapter.getData().get(position).pinnedHeaderName, Toast.LENGTH_SHORT).show();
                       break;
                   case R.id.iv_more:
                       Toast.makeText(StockActivity.this, "click " + mAdapter.getData().get(position).pinnedHeaderName + "'s more button", Toast.LENGTH_SHORT)
                             .show();
                       break;
                   case R.id.checkbox:
                       final CheckBox checkBox = (CheckBox) view;
                       checkBox.setChecked(!checkBox.isChecked());
                       // invalidate ItemDecorations to draw the header
                       mRecyclerView.invalidateItemDecorations();

                       mAdapter.getData().get(position).check = checkBox.isChecked();
                       mAdapter.notifyItemChanged(position + mHeaderItemDecoration.getDataPositionOffset());

                       break;
               }
           }

       };

     mRecyclerView.addItemDecoration(
             // Set the type of pinned header
             new PinnedHeaderItemDecoration.Builder(StockEntity.StockInfo.TYPE_HEADER)
             // Set separator line resources id.
             .setDividerId(R.drawable.divider)
             // Enable draw the separator line, by default it's disable.
             .enableDivider(true)
             // Set click event for the header and its internal child view.
             .setClickIds(R.id.iv_more)
             // Disable header click event， by default it's enable.
             .disableHeaderClick(false)
             // Set the listener. If the listener is not null but disable the header click event(eg. disableHeaderClick(true)), then the callback don't return.
             .setHeaderClickListener(clickAdapter)
             .create());
    
```
![大标签布局](/pic/big_pinned_header.png) 

To achieve small pinned section header is a little bit more complex, such as the layout A of the data .
```
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
             xmlns:tools="http://schemas.android.com/tools"
             android:layout_width="match_parent"
             android:layout_height="wrap_content"
             android:background="#70E593">

    <ImageView
        android:id="@+id/iv_animal"
        android:layout_gravity="center"
        android:layout_width="match_parent"
        android:layout_height="120dp"
        tools:src="@mipmap/panda0"/>

    <TextView
        android:id="@+id/tv_pos"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:padding="8dp"
        android:textColor="#000000"
        android:textSize="18dp"
        tools:text="1"/>

</FrameLayout>
```
![布局A](/pic/item-data.png) 

This is a layout B with a small pinned section header.
```
<FrameLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">

    <ImageView
        android:id="@+id/iv_animal"
        android:layout_width="match_parent"
        android:layout_height="120dp"
        android:background="#70E593"
        tools:src="@mipmap/panda0"/>

    <ImageView
        android:id="@+id/iv_small_pinned_header"
        android:layout_width="50dp"
        android:layout_height="50dp"
        android:background="#5A5A5A"
        android:padding="8dp"
        android:textColor="#ffffff"
        android:textSize="18dp"
        tools:src="@mipmap/panda0"
        tools:text="熊猫"/>

</FrameLayout>
```
![布局B](/pic/small_pinned_header.png) 

The layout B add a header compare whit layout A，then RecyclerView only need to add a SmallPinnedHeaderItemDecoration. Note that the top layer of the header where the layout can not be set marginTop.「[See SecondActivity](https://github.com/oubowu/PinnedSectionItemDecoration/blob/master/app%2Fsrc%2Fmain%2Fjava%2Fcom%2Foushangfeng%2Fpinneddemo%2FSecondActivity.java#L114-L126)」
```
     OnHeaderClickAdapter headerClickAdapter = new OnHeaderClickAdapter() {

          @Override
          public void onHeaderClick(View view, int id, int position) {
              if (id == R.id.iv_small_pinned_header) {
                  Toast.makeText(SecondActivity.this, "click tag: " + mAdapter.getData().get(position).getPinnedHeaderName(), Toast.LENGTH_SHORT).show();
              }
          }
     };
     mRecyclerView.addItemDecoration(
             // Constructor need to set the id and type of the header 
             new SmallPinnedHeaderItemDecoration.Builder(R.id.tv_small_pinned_header,BaseHeaderAdapter.TYPE_HEADER)
             // Enable draw the separator line, by default it's disable.
             .enableDivider(true)
             // Set separator line resources id.
             .setDividerId(R.drawable.divider)
             // Set click event for the header and its internal child view.
             .setClickIds(R.id.tv_small_pinned_header)
             // Disable header click event， by default it's enable.
             .disableHeaderClick(false)
             // Set the listener. If the listener is not null but disable the header click event(eg. disableHeaderClick(true)), then the callback don't return.
             .setHeaderClickListener(clickAdapter)
             .create());
    
```

#### License
```
Copyright 2016 oubowu

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```




