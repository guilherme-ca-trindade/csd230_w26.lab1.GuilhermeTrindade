import BoardGame from './BoardGame.jsx';

// Lab 5: presentation only, the same split BookList uses. App owns the collection and the
// fetch; this component just reports loading, error and empty states and maps the saved
// records to rows keyed by their database id.
export default function BoardGameList({ boardGames, loading, error, onDelete, onUpdate }) {
  if (loading) return <p>Loading board games...</p>;

  return <section aria-label="Board game inventory">
    <h2>Board Games</h2>
    {error && <p role="alert">{error}</p>}
    {!error && boardGames.length === 0 && <p>No board games yet.</p>}
    <div className="boardgame-list">
      {boardGames.map(boardGame => <BoardGame key={boardGame.id} boardGame={boardGame}
        onDelete={onDelete} onUpdate={onUpdate} />)}
    </div>
  </section>;
}
