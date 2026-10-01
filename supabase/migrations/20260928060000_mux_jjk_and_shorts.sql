insert into public.creators (id, display_name, bio, can_publish) values
    ('og123', 'OG123', 'Uploads hosted on Mux.', false);

insert into public.series (id, creator_id, title, description, cover_url, genres, status, created_at) values
    (
        'jjk',
        'og123',
        'JJK',
        'Two episodes hosted on Mux.',
        'https://image.mux.com/6Tx7rD01KmWlfwOqEpHtghxVFC2vMfXPjEokm01cGN4RI/thumbnail.jpg',
        array['Animation'],
        'ONGOING',
        '2026-09-28T00:00:00Z'
    );

insert into public.films (
    id, creator_id, series_id, episode_number, title, description, thumbnail_url, video_url,
    duration_seconds, published_at
) values
    (
        'jjk-ep1', 'og123', 'jjk', 1, 'Yuta', 'Episode 1. Mux title JJK.',
        'https://image.mux.com/6Tx7rD01KmWlfwOqEpHtghxVFC2vMfXPjEokm01cGN4RI/thumbnail.jpg',
        'https://stream.mux.com/6Tx7rD01KmWlfwOqEpHtghxVFC2vMfXPjEokm01cGN4RI.m3u8',
        241, '2026-09-28T04:22:34Z'
    ),
    (
        'jjk-ep2', 'og123', 'jjk', 2, 'Suki', 'Episode 2. Mux title JJK.',
        'https://image.mux.com/6LiNulQ3c7uKlCK1bzvWjK4BfPwG7KxeNZauZNITdQY/thumbnail.jpg',
        'https://stream.mux.com/6LiNulQ3c7uKlCK1bzvWjK4BfPwG7KxeNZauZNITdQY.m3u8',
        168, '2026-09-28T04:24:58Z'
    ),
    (
        'oneoff-music', 'og123', null, null, 'MusicTest', 'Standalone short hosted on Mux.',
        'https://image.mux.com/gEIsW3u5ZhNERRb01yd73QReSeV02t2V5UBKuXXWIdVYs/thumbnail.jpg',
        'https://stream.mux.com/gEIsW3u5ZhNERRb01yd73QReSeV02t2V5UBKuXXWIdVYs.m3u8',
        252, '2026-09-28T04:26:10Z'
    ),
    (
        'oneoff-ai', 'og123', null, null, 'AI video', 'Standalone short hosted on Mux.',
        'https://image.mux.com/wC5lxvyKyo2JjIzR2QY3H7O7pYDU1HkaN6zyrRHyyp8/thumbnail.jpg',
        'https://stream.mux.com/wC5lxvyKyo2JjIzR2QY3H7O7pYDU1HkaN6zyrRHyyp8.m3u8',
        641, '2026-09-28T04:25:52Z'
    );
