# Direction — AI video platform

> Planning document for the next product direction. Drive later phases from this file.
> `PROJECT.md` remains the record of the single-creator animation prototype that already shipped.
> Last updated: 2026-09-27
> Status: decisions below are **Confirmed** by Matt. The product name (N12) is still open.

## 1. The shift

The prototype is a personal streaming channel: one creator publishes series, everyone else watches.

The new direction is a **streaming service with a creator community, built for people who watch and make AI video**. Many creators publish. Viewers browse, follow, and watch. The product should feel like a place that wants this work, not a general network that tolerates it.

"Pro-AI" here means two things at once:

- The audience and the makers are people who use AI to make video and want to watch it.
- The bar is closer to a studio reel than a raw generation dump. Presentation, credit, and a real shelf (series and single films) matter.

It does not mean the app generates video. Generation stays in Runway, Kling, Luma, and whatever tool the creator already uses. This product is where the finished piece lives and finds people.

## 2. Recommendation

**Keep the streaming spine. Add creators. Add a thin social layer. Do not become a short-form feed.**

The watch experience already exists: home, series, player, resume, auto-next. That is the expensive part of a streaming app, and it is the part generic social clones do not have. A For You feed of 15-second clips puts the product in a direct fight with TikTok, YouTube Shorts, and Instagram. That fight is about distribution and moderation scale, not about the player you already built.

The differentiated product is:

> A studio shelf for AI video. Creators publish series and one-off films or clips. Viewers follow creators and pick up where they left off. Discovery is a featured row plus who you follow.

Social features earn their place when they help someone find the next series or tell a maker the work landed. Follows, a Following shelf, and likes do that. Comments, reposts, duets, and a ranked algorithmic feed can wait until there is something worth commenting on.

### What to keep from the prototype

- Kotlin Multiplatform + Compose Multiplatform for Android and iOS, shared UI.
- Series → episodes for ongoing work. One-off films and clips are their own items, with no series attached.
- The player (resume, auto-next inside a series, fullscreen) and local watch progress.
- Repository interfaces, so screens do not care whether data is fake or remote.
- The design system. The visual direction can stay; the words "anime platform" cannot.

### What changes

| Prototype assumption | New assumption |
|---|---|
| One creator, hidden admin mode | Many creators, a real Creator area in the app |
| Admin flag in local settings | Accounts later; until then, a local "signed in as creator" is only a prototype |
| In-memory catalog, lost on restart | Same for the next UI slice, then a real backend |
| Viewers do not publish | Anyone allowed to publish can upload |
| Anime-only framing | AI video and AI-assisted animation. Anime can be a genre inside the catalog |
| Everything is a series | Series for episodic work. One-off films and clips publish on their own |
| Upload is a phone admin form | Phone creator page now; desktop web upload is the serious publishing tool later |

## 3. How this is different

Most places a maker can put AI video are hostile, generic, or tool-shaped.

| Place | What it is good at | Gap |
|---|---|---|
| YouTube, TikTok, Instagram | Reach | AI work is buried, demonetized, or treated as spam. No series-first shelf. |
| Runway, Kling, Luma, Sora | Making the clip | Not a library, not a community, not a player with resume and series. |
| Civitai and similar | Models and stills, heavy community | Not a polished long-form watch experience. Reputation is not "pro film." |
| Vimeo OTT, Uscreen, Nebula | Owned, quality video | Not built around AI makers, credits, or a multi-creator social graph. |

The opening is the overlap nobody owns well: **finished AI films and clips, some gathered into series, by named creators, for an audience that asked for this.**

Concrete differences to protect:

1. **A shelf, including singles.** Home shows series and one-off films side by side. A clip opens like a film. It does not become a swipe stack.
2. **Credits are part of the post.** Model, tool, and "AI-assisted vs fully generated" are fields, not a caption afterthought. That is both identity and trust.
3. **Quality is a gate, lightly.** Invite, application, or a visible "featured" tier. Open upload with no bar becomes a slop folder and kills the "pro" claim.
4. **Watch state matters.** Resume and continue-watching are the features a social feed does not bother with. They signal "this is something you sit with."

## 4. Decisions

Confirmed by Matt on 2026-09-27. N12 still needs a name. Build Step A against this table.

| # | Decision | Confirmed choice | Why |
|---|---|---|---|
| N1 | Primary loop | **Catalog plus follows** | Uses the player you have. Follows help people find work. |
| N2 | Who can publish | **Invite or application for any public release** | The prototype still uses fake creators. A public free-for-all drops the "pro" bar. |
| N3 | Content shape | **Series with episodes, and one-off films/clips with no series** | A single piece must be publishable on its own. Wrapping it in a fake one-episode series is the wrong object. |
| N4 | What must be labeled | **Tool, model, and an origin flag** (fully generated, AI-assisted, unclear) | Viewers came for AI work and still need to know what they are watching. |
| N5 | Social in the first public version | **Follows and likes. Comments stay out.** | Comments are a moderation product. Follows change the home screen. |
| N6 | Accounts | **Anonymous watch; account to follow, like, or upload** | The first play should not require sign-in. |
| N7 | First upload UI | **Mobile creator page on the fake repository** | This is the next build. Web waits until the upload fields settle. |
| N8 | Web client, later | **Separate web app on the same API** | Desktop upload and a public watch link are the job. Sharing every Compose screen is the slower path. |
| N9 | Backend timing | **Fake multi-creator repository first, Supabase second** | The upload screen will change. Transcoding spend waits until the fields settle. |
| N10 | Video host | **Mux or Cloudflare Stream** | Many creators means transcoding, abuse handling, and adaptive playback. |
| N11 | Monetization | **Free while this is a prototype. Revisit before any public store listing.** | Tips and subscriptions change store rules and the data model. |
| N12 | Name | **Retire "Anime Platform." The replacement name is still open.** | Anime can remain a genre. |
| N13 | Adult content | **Banned until a later, explicit decision** | Stranger-uploaded AI video will force this question. It stays closed until you open it. |
| N14 | Likeness and copyright | **Ban real-person deepfakes and obvious third-party characters in the community rules** | Enforcement tooling can come later. The rule exists now. |
| N15 | Discovery | **Featured row plus Following** | A ranked For You feed waits until there is a catalog and a way to review what it promotes. |

### Content model (N3)

One playable type, called a **film** in the product. A short clip is the same object with a shorter duration. There is no third "post" type.

```text
Creator
  id, displayName, avatarUrl, bio

Series                          // optional container
  id, creatorId, title, description, coverUrl, genres, status, createdAt

Film                            // the thing you play
  id, creatorId
  seriesId?                     // null means one-off
  episodeNumber?                // set only when seriesId is set
  title, description, thumbnailUrl, videoUrl, durationSeconds
  tools, modelName, origin      // origin: FULLY_GENERATED, AI_ASSISTED, UNCLEAR
  publishedAt
```

Rules:

- A one-off has `seriesId = null`. It gets its own poster, its own page, and its own place on Home and in the creator's list.
- An episode is a film whose `seriesId` is set. Auto-next applies only inside that series.
- Deleting a series deletes its episodes. Deleting a one-off deletes that film only.
- Watch progress is stored per film.
- Home can feature either a series or a film. Continue watching is always a film.

### Decisions that still hold

- Kotlin Multiplatform and Compose Multiplatform for the mobile apps (old D1).
- Video, not comics or image posts, as the thing you watch (old D2). Stills can appear as covers and profile images.
- MVVM and a repository interface (old D5).
- Shared player via `expect`/`actual`, Media3 and AVPlayer (old D10).
- Coil 3 for images (old D11).
- Type-safe navigation (old D12).

Old **D3** (only one creator publishes) is retired. The next screens are a Creator area, not a hidden admin mode for a single owner.

## 5. Phases

Each phase is demoable on its own. Later phases assume the earlier ones landed.

Today the code only knows `Series` and `Episode`, and `seriesId` is required. Home, the player, and the admin editors all assume that. The creator studio waits until a one-off can exist without breaking playback.

| Phase | Name | Demo when it is done |
|---|---|---|
| **6** | One-offs in the catalog | Home shows series and standalone films from several fake creators. A one-off plays. |
| **7** | Creator studio | A visible Creator area can publish a one-off, a series, or an episode. The long-press admin path is gone. |
| **8** | Follows and likes | A viewer follows a creator. Home has a Following shelf. A series or film can be liked. Still local. |
| **9** | Backend and real upload | Sign-in, server-side writes scoped to the creator, video hosted on Mux or Cloudflare Stream. |
| **10** | Web companion | Desktop site uploads and manages the same catalog, and a link plays without the app. |

### Phase 6 — One-offs in the catalog

**Done.** Android and iOS simulator targets compile, and `testAndroidHostTest` passes, including seed creators, one-off create/delete, and next-episode rules. Home has a Films row and creator names. One-offs come from seed data; the admin editors still only create series and episodes.

Make the catalog match N3 far enough that a viewer can see and play a film that has no series. Publishing stays on the existing admin editors, which still only create series and episodes.

In scope:

- Add `Creator` (`id`, `displayName`, `avatarUrl`, `bio`).
- Put `creatorId` on `Series` and on the playable item.
- Make `seriesId` and episode `number` nullable. Null `seriesId` means a one-off.
- Add `tools`, `modelName`, and `origin` on the playable item now, so Phase 7 does not migrate the model again. Seed data may leave them empty. The upload form does not ask for them yet.
- Rename `Episode` to `Film` in this pass. The player, progress, and routes should use the word the product uses. `episodeNumber` is only set when `seriesId` is set.
- Repository: list one-offs, create a one-off, keep series CRUD. Deleting a series still deletes its films. Deleting a one-off deletes that film only.
- Seed two or three creators. Keep the existing series. Add at least two one-offs that reuse the Archive.org videos already in the seed.
- Home: creator name on series cards, a Films row of one-offs, tap a one-off to open a thin detail (title, creator, description, play) and then the existing player.
- Auto-next only when the film belongs to a series and a later episode exists.
- Update unit tests for nullable series membership and one-off create/delete.

Out of scope:

- Creator studio UI, **New film** / **New series** as a public destination, and removing the admin toggle. That is Phase 7.
- Follows, likes, accounts, real upload, web.
- A new name for the app (N12).

### Phase 7 — Creator studio

**Done.** Home has a Studio button for the prototype creator (Pampas Pictures). From there: New film, New series, Add episode, edit, and delete. One-off films ask for video, cover, title, description, tools, model, and origin. Episode forms carry the same credit fields. The long-press admin toggle is gone. Files stay on-device.

Turn the hidden admin editors into the studio desk in section 6.

- Creator destination listing that creator's one-offs and series.
- **New film** (no series), **New series**, **Add episode**.
- N4 fields on the form: tools, model, origin.
- One publishing path. The long-press admin toggle becomes a dev switch or goes away.
- Local files only, same as admin mode today.

### Phase 8 — Follows and likes

**Done.** Follows and likes are stored on the device. Follow a creator from the featured banner, a series page, or a film page. Home then shows a Following shelf of that creator's series and one-offs. Like toggles on a series or a film. Comments stay out.

- Local follow and like state.
- Following shelf on Home beside Featured and Continue watching.
- Likes on a series or a film.
- Comments stay out.

### Phase 9 — Backend and real upload

**Database is up. The app still uses the fake repository.**

Supabase project **Mateo610's Project** (`yxyuquuyljgbxtzkydju`, `us-east-2`): `https://yxyuquuyljgbxtzkydju.supabase.co`. Schema is in `supabase/migrations/20260928050000_catalog_follows_likes.sql`. Anonymous clients can read creators, series, and films. Follows and likes require a signed-in user, and only that user's rows. Publishing requires `creators.can_publish`, which is false until you grant an invite. A new auth user gets a creator row automatically and still cannot publish. Seed catalog matches the app: 3 creators, 4 series, 8 films (2 one-offs). The database password is not stored in the repo.

Mux playback for the four uploaded films is in the catalog (JJK episodes Yuta and Suki, plus MusicTest and AI video). The Mux access token is in `.env` and is not committed. Still to do: sign-in in the app, point the repositories at Supabase, and mint direct uploads from the studio instead of pasting a finished playback URL.

- Supabase (or equivalent) for accounts, creators, series, films, follows.
- Row-level security: a creator writes only their rows.
- Direct resumable upload to Mux or Cloudflare Stream. The app stores a playback id.
- The phone creator page calls the API the web app will use.

### Phase 10 — Web companion

- Desktop upload and management for series and one-offs.
- A public watch page for a shared link.
- Starts only after the Phase 7 form has stopped changing.

## 5.1 Next session

Wire the app to Supabase: sign-in, then replace the fake catalog and on-device follows with the tables already in Mateo610's Project. Video upload to Mux or Cloudflare Stream comes after that. The database password stays out of git.

## 6. Creator page — product notes for Step A

The page is a studio desk, not a social profile with a camera button.

- Header: creator name and a short line. Avatar can be a placeholder.
- List: their one-off films and their series (status, episode count).
- Primary actions: **New film** and **New series**. From a series, **Add episode**.
- A new film asks for video, cover, title, description, tools, model, and origin. It does not ask which series.
- Fields from N4 stay short. A paragraph of prompt text stays off the default form.
- Empty state: one sentence about publishing a film or a series, then those two actions.
- Failure states: pick cancelled, duration unreadable, missing title. The current editors already cover most of this.

Phone upload is for proving the flow and for short pieces. Say so in the UI once web exists ("Large files are easier on the web"). Do not say it before the web exists.

## 7. Risks to decide with eyes open

- **Slop flood.** Open upload makes the home screen worse every day. N2 is the mitigation.
- **Trust and stores.** User-generated AI video triggers review for copyright, sexual content, and real-person likeness. N13 and N14 need a yes before any public TestFlight or Play listing that allows strangers to upload.
- **Cost.** Transcoding and delivery scale with minutes watched and minutes uploaded. N10 plus a cap on film length in the prototype (for example 20 minutes) keeps a surprise bill from being the plan.
- **Two clients.** Web plus mobile before one upload contract exists means fixing every product mistake twice.
- **Old brand in the code.** "Anime Platform," the welcome line, and the admin gesture will keep pulling the product back to the single-creator anime channel until Step A replaces them. Name can wait; the creator path should not.

## 8. Not in this direction

- Building or hosting a video generation model.
- A short-form duet/remix format.
- Comments, DMs, and notifications.
- Payments.
- Replacing the mobile codebase with a website.
- Offline downloads.

## 9. Locked before Phase 6

1. Catalog plus follows (N1). One-off clips are catalog items you open and play.
2. Invite-only publishing for any public release. The prototype uses fake creators (N2).
3. Series with episodes, plus one-off films and clips that have no series (N3).
4. Adult content and real-person likeness stay banned until a later decision (N13, N14).
5. Mobile creator page next, on fake data. Web and Supabase after the upload fields settle (N7, N8, N9).
