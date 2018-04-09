package net.mls.modelserving;

/**
 * Created by char on 4/9/18.
 */
public class MovieView {
    private String title;
    private Double rating;

    public MovieView(String title, Double rating) {
        this.title = title;
        this.rating = rating;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MovieView movieView = (MovieView) o;

        if (title != null ? !title.equals(movieView.title) : movieView.title != null) return false;
        return rating != null ? rating.equals(movieView.rating) : movieView.rating == null;
    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (rating != null ? rating.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MovieView{" +
                "title='" + title + '\'' +
                ", rating=" + rating +
                '}';
    }
}
