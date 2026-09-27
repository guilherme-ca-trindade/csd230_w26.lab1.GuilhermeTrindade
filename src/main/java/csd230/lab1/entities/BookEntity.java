package csd230.lab1.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.util.Objects;

@Entity @DiscriminatorValue("BOOK")
public class BookEntity extends PublicationEntity {
    private String author;
    private String isbn;

    public BookEntity() {}
    public BookEntity(String t, double p, int c, String a) { super(t, p, c); this.author = a; }
    public BookEntity(String t, double p, int c, String a, String isbn) {
        super(t, p, c);
        this.author = a;
        this.isbn = isbn;
    }

    public String getAuthor() { return author; }
    public void setAuthor(String a) { this.author = a; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    @Override public String toString() {
        return "Book{author='" + author + "', isbn='" + isbn + "', " + super.toString() + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookEntity other)) return false;
        if (getId() != null || other.getId() != null) return super.equals(o);
        return Objects.equals(author, other.author) && Objects.equals(isbn, other.isbn) && super.equals(o);
    }

    @Override public int hashCode() { return super.hashCode(); }
}
