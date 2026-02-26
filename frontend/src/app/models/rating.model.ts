export interface Rating {
  ratingId: string;
  movieId: string;
  movieTitle: string;
  userId: string;
  score: number;
  comment: string;
  createdAt: string;
}

export interface CreateRatingRequest {
  movieId: string;
  score: number;
  comment: string;
}
