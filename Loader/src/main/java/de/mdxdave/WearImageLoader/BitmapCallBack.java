package de.mdxdave.WearImageLoader;

public abstract class BitmapCallBack implements WearImageLoader.Callback{
    private Object into;

    public BitmapCallBack(Object into) {
        this.into = into;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BitmapCallBack that = (BitmapCallBack) o;

        if (into != null ? !into.equals(that.into) : that.into != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return into != null ? into.hashCode() : 0;
    }

}
