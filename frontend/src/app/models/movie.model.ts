export interface Movie {
  movieId: string;
  title: string;
  year: number;
  synopsis: string;
  posterUrl: string;
  averageRating: number;
  totalRatings: number;
  genres: Genre[];
  directors: Director[];
  actors: Actor[];
}

export interface Genre {
  name: string;
}

export interface Director {
  name: string;
}

export interface Actor {
  name: string;
  character: string;
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
