-- Catalog, follows, and likes for the AI video app.
-- Anonymous viewers can read the catalog. Signed-in users follow and like.
-- Publishing requires creators.can_publish, which stays false until an invite is granted.

create schema if not exists private;

revoke all on schema private from public;
revoke all on schema private from anon, authenticated;

create table public.creators (
    id text primary key default gen_random_uuid()::text,
    user_id uuid unique references auth.users (id) on delete cascade,
    display_name text not null,
    avatar_url text not null default '',
    bio text not null default '',
    can_publish boolean not null default false,
    created_at timestamptz not null default now()
);

create table public.series (
    id text primary key default gen_random_uuid()::text,
    creator_id text not null references public.creators (id) on delete cascade,
    title text not null,
    description text not null default '',
    cover_url text not null default '',
    genres text[] not null default '{}',
    status text not null check (status in ('ONGOING', 'COMPLETED', 'HIATUS')),
    attribution text not null default '',
    created_at timestamptz not null default now()
);

create table public.films (
    id text primary key default gen_random_uuid()::text,
    creator_id text not null references public.creators (id) on delete cascade,
    series_id text references public.series (id) on delete cascade,
    episode_number integer,
    title text not null,
    description text not null default '',
    thumbnail_url text not null default '',
    video_url text not null default '',
    duration_seconds integer not null default 0 check (duration_seconds >= 0),
    published_at timestamptz not null default now(),
    tools text not null default '',
    model_name text not null default '',
    origin text not null default 'UNCLEAR' check (origin in ('FULLY_GENERATED', 'AI_ASSISTED', 'UNCLEAR')),
    intro_start_seconds integer,
    intro_end_seconds integer,
    constraint films_one_off_or_episode check (
        (series_id is null and episode_number is null)
        or (series_id is not null and episode_number is not null and episode_number > 0)
    ),
    constraint films_intro_range check (
        (intro_start_seconds is null and intro_end_seconds is null)
        or (
            intro_start_seconds is not null
            and intro_end_seconds is not null
            and intro_start_seconds >= 0
            and intro_end_seconds > intro_start_seconds
        )
    )
);

create unique index films_series_episode_unique
    on public.films (series_id, episode_number)
    where series_id is not null;

create index series_creator_id_idx on public.series (creator_id);
create index films_creator_id_idx on public.films (creator_id);
create index films_series_id_idx on public.films (series_id);

create table public.follows (
    user_id uuid not null references auth.users (id) on delete cascade,
    creator_id text not null references public.creators (id) on delete cascade,
    created_at timestamptz not null default now(),
    primary key (user_id, creator_id)
);

create index follows_creator_id_idx on public.follows (creator_id);

create table public.series_likes (
    user_id uuid not null references auth.users (id) on delete cascade,
    series_id text not null references public.series (id) on delete cascade,
    created_at timestamptz not null default now(),
    primary key (user_id, series_id)
);

create table public.film_likes (
    user_id uuid not null references auth.users (id) on delete cascade,
    film_id text not null references public.films (id) on delete cascade,
    created_at timestamptz not null default now(),
    primary key (user_id, film_id)
);

alter table public.creators enable row level security;
alter table public.series enable row level security;
alter table public.films enable row level security;
alter table public.follows enable row level security;
alter table public.series_likes enable row level security;
alter table public.film_likes enable row level security;

create policy creators_select_public
    on public.creators
    for select
    to anon, authenticated
    using (true);

create policy creators_update_own
    on public.creators
    for update
    to authenticated
    using (user_id = (select auth.uid()))
    with check (user_id = (select auth.uid()));

grant select on public.creators to anon, authenticated;
grant update (display_name, avatar_url, bio) on public.creators to authenticated;

create policy series_select_public
    on public.series
    for select
    to anon, authenticated
    using (true);

create policy series_insert_own
    on public.series
    for insert
    to authenticated
    with check (
        exists (
            select 1
            from public.creators
            where creators.id = series.creator_id
              and creators.user_id = (select auth.uid())
              and creators.can_publish
        )
    );

create policy series_update_own
    on public.series
    for update
    to authenticated
    using (
        exists (
            select 1
            from public.creators
            where creators.id = series.creator_id
              and creators.user_id = (select auth.uid())
              and creators.can_publish
        )
    )
    with check (
        exists (
            select 1
            from public.creators
            where creators.id = series.creator_id
              and creators.user_id = (select auth.uid())
              and creators.can_publish
        )
    );

create policy series_delete_own
    on public.series
    for delete
    to authenticated
    using (
        exists (
            select 1
            from public.creators
            where creators.id = series.creator_id
              and creators.user_id = (select auth.uid())
              and creators.can_publish
        )
    );

grant select on public.series to anon, authenticated;
grant insert, update, delete on public.series to authenticated;

create policy films_select_public
    on public.films
    for select
    to anon, authenticated
    using (true);

create policy films_insert_own
    on public.films
    for insert
    to authenticated
    with check (
        exists (
            select 1
            from public.creators
            where creators.id = films.creator_id
              and creators.user_id = (select auth.uid())
              and creators.can_publish
        )
    );

create policy films_update_own
    on public.films
    for update
    to authenticated
    using (
        exists (
            select 1
            from public.creators
            where creators.id = films.creator_id
              and creators.user_id = (select auth.uid())
              and creators.can_publish
        )
    )
    with check (
        exists (
            select 1
            from public.creators
            where creators.id = films.creator_id
              and creators.user_id = (select auth.uid())
              and creators.can_publish
        )
    );

create policy films_delete_own
    on public.films
    for delete
    to authenticated
    using (
        exists (
            select 1
            from public.creators
            where creators.id = films.creator_id
              and creators.user_id = (select auth.uid())
              and creators.can_publish
        )
    );

grant select on public.films to anon, authenticated;
grant insert, update, delete on public.films to authenticated;

create policy follows_select_own
    on public.follows
    for select
    to authenticated
    using (user_id = (select auth.uid()));

create policy follows_insert_own
    on public.follows
    for insert
    to authenticated
    with check (
        user_id = (select auth.uid())
        and not exists (
            select 1
            from public.creators
            where creators.id = follows.creator_id
              and creators.user_id = (select auth.uid())
        )
    );

create policy follows_delete_own
    on public.follows
    for delete
    to authenticated
    using (user_id = (select auth.uid()));

grant select, insert, delete on public.follows to authenticated;

create policy series_likes_select_own
    on public.series_likes
    for select
    to authenticated
    using (user_id = (select auth.uid()));

create policy series_likes_insert_own
    on public.series_likes
    for insert
    to authenticated
    with check (user_id = (select auth.uid()));

create policy series_likes_delete_own
    on public.series_likes
    for delete
    to authenticated
    using (user_id = (select auth.uid()));

grant select, insert, delete on public.series_likes to authenticated;

create policy film_likes_select_own
    on public.film_likes
    for select
    to authenticated
    using (user_id = (select auth.uid()));

create policy film_likes_insert_own
    on public.film_likes
    for insert
    to authenticated
    with check (user_id = (select auth.uid()));

create policy film_likes_delete_own
    on public.film_likes
    for delete
    to authenticated
    using (user_id = (select auth.uid()));

grant select, insert, delete on public.film_likes to authenticated;

create or replace function private.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
    insert into public.creators (user_id, display_name)
    values (
        new.id,
        coalesce(nullif(split_part(new.email, '@', 1), ''), 'Creator')
    );
    return new;
end;
$$;

revoke all on function private.handle_new_user() from public, anon, authenticated;

create trigger on_auth_user_created
    after insert on auth.users
    for each row
    execute function private.handle_new_user();

insert into public.creators (id, display_name, bio, can_publish) values
    ('pampas', 'Pampas Pictures', 'Short comedies about animals who refuse to learn.', false),
    ('peach', 'Peach Tree', 'Bright forest films and one-off experiments.', false),
    ('relay', 'Relay', 'Fantasy and mechanical worlds.', false);

insert into public.series (id, creator_id, title, description, cover_url, genres, status, attribution, created_at) values
    ('caminandes', 'pampas', 'Caminandes', 'Koro the stubborn llama battles the harsh Patagonian landscape, one bad idea at a time.', 'https://archive.org/services/img/CaminandesLlamigos', array['Comedy', 'Animation'], 'COMPLETED', 'Caminandes © Blender Foundation | caminandes.com | CC BY 3.0', '2013-01-01T00:00:00Z'),
    ('bbb', 'peach', 'Big Buck Bunny', 'A large-hearted rabbit takes on three bullying rodents in a lush forest.', 'https://archive.org/services/img/BigBuckBunny_124', array['Comedy', 'Animation'], 'COMPLETED', 'Big Buck Bunny © Blender Foundation | CC BY 3.0', '2010-01-01T00:00:00Z'),
    ('sintel', 'relay', 'Sintel', 'A young woman searches for a dragon she raised from infancy.', 'https://archive.org/services/img/Sintel', array['Fantasy', 'Adventure', 'Animation'], 'COMPLETED', 'Sintel © Blender Foundation | CC BY 3.0', '2010-01-01T00:00:00Z'),
    ('elephants-dream', 'relay', 'Elephants Dream', 'Two men explore a surreal mechanical world and the secrets it hides.', 'https://archive.org/services/img/ElephantsDream', array['Sci-Fi', 'Animation'], 'COMPLETED', 'Elephants Dream © Blender Foundation / Netherlands Media Art Institute | CC BY 2.5', '2010-01-01T00:00:00Z');

insert into public.films (
    id, creator_id, series_id, episode_number, title, description, thumbnail_url, video_url,
    duration_seconds, published_at, intro_start_seconds, intro_end_seconds
) values
    ('caminandes-ep1', 'pampas', 'caminandes', 1, 'Llama Drama', 'Koro wants the grass on the other side of the road. The road has other plans.', 'https://archive.org/services/img/Caminandes1LlamaDrama', 'https://archive.org/download/Caminandes1LlamaDrama/01_llama_drama_1080p.mp4', 90, '2013-01-01T00:00:00Z', 0, 8),
    ('caminandes-ep2', 'pampas', 'caminandes', 2, 'Gran Dillama', 'An electric fence stands between Koro and the greener pastures beyond.', 'https://archive.org/services/img/Caminandes2GranDillama', 'https://archive.org/download/Caminandes2GranDillama/02_gran_dillama_1080p.mp4', 146, '2013-11-01T00:00:00Z', null, null),
    ('caminandes-ep3', 'pampas', 'caminandes', 3, 'Llamigos', 'Winter arrives, berries are scarce, and Koro meets a penguin with the same idea.', 'https://archive.org/services/img/CaminandesLlamigos', 'https://archive.org/download/CaminandesLlamigos/Caminandes_%20Llamigos-1080p.mp4', 150, '2016-01-29T00:00:00Z', null, null),
    ('bbb-ep1', 'peach', 'bbb', 1, 'Big Buck Bunny', 'The full open movie.', 'https://archive.org/services/img/BigBuckBunny_124', 'https://archive.org/download/BigBuckBunny_124/Content/big_buck_bunny_720p_surround.mp4', 596, '2010-01-01T00:00:00Z', null, null),
    ('sintel-ep1', 'relay', 'sintel', 1, 'Sintel', 'The full open movie.', 'https://archive.org/services/img/Sintel', 'https://archive.org/download/Sintel/sintel-2048-surround.mp4', 888, '2010-01-01T00:00:00Z', null, null),
    ('elephants-dream-ep1', 'relay', 'elephants-dream', 1, 'Elephants Dream', 'The full open movie.', 'https://archive.org/services/img/ElephantsDream', 'https://archive.org/download/ElephantsDream/ed_hd.mp4', 653, '2010-01-01T00:00:00Z', null, null),
    ('oneoff-fence', 'peach', null, null, 'The Fence', 'A standalone short. Gran Dillama © Blender Foundation | CC BY 3.0.', 'https://archive.org/services/img/Caminandes2GranDillama', 'https://archive.org/download/Caminandes2GranDillama/02_gran_dillama_1080p.mp4', 146, '2013-11-01T00:00:00Z', null, null),
    ('oneoff-search', 'pampas', null, null, 'The Search', 'A standalone film. Sintel © Blender Foundation | CC BY 3.0.', 'https://archive.org/services/img/Sintel', 'https://archive.org/download/Sintel/sintel-2048-surround.mp4', 888, '2010-01-01T00:00:00Z', null, null);
