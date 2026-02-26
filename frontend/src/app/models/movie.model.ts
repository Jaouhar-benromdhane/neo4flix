export interface Movie {
  movieId: string;
  title: string;
  releaseYear: number;
  synopsis: string;
  posterUrl: string;
  language: string;
  duration: number;
  averageRating: number;
  totalRatings: number;
  genres: string[];
  directors: string[];
  actors: string[];
}

export interface CreateMovieRequest {
  title: string;
  year: number;
  synopsis: string;
  posterUrl: string;
  genreNames: string[];
  directorNames: string[];
  actorNames: string[];
}
