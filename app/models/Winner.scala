package models

import countvotes.structures.Candidate

case class Winner(
  candidate : Candidate,
  score : Score
)
