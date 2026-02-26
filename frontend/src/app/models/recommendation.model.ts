export interface Recommendation {
  movieId: string;
  title: string;
  year: number;
  synopsis: string;
  posterUrl: string;
  averageRating: number;
  totalRatings: number;
  relevanceScore: number;
  reason: string;
}
