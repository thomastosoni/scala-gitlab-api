package models

case class TeamMember(username: String,
                      email: String,
                      name: String,
                      state: String,
                      created_at: String,
                      access_level: Int)