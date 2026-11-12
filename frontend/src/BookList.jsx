import Book from './Book.jsx';

export default function BookList({ books, loading, error, onDelete, onUpdate }) {
  if (loading) return <p>Loading books...</p>;

  return <section aria-label="Book inventory">
    <h2>Books</h2>
    {error && <p role="alert">{error}</p>}
    {!error && books.length === 0 && <p>No books yet.</p>}
    {books.map(book => <Book key={book.id} book={book} onDelete={onDelete} onUpdate={onUpdate} />)}
  </section>;
}
