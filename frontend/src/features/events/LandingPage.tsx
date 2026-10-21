import { listVisibleEvents } from '@/api/events'
import { PassGrid } from '@/components/pass/PassGrid'
import { PassSkeleton } from '@/components/pass/PassSkeleton'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'

const paths = [
  { number: '01', title: 'Hackathons', detail: 'Build something that matters', type: 'TEAM' },
  { number: '02', title: 'Individual challenges', detail: 'Put your skills to the test', type: 'SOLO' },
  { number: '03', title: 'Find your people', detail: 'Create a team. Make it happen.', type: 'TEAM' },
]

export function LandingPage() {
  const { data = [], isLoading, isError } = useQuery({
    queryKey: ['events', 'visible'],
    queryFn: listVisibleEvents,
  })
  const featured = data.filter((event) => event.status === 'PUBLISHED').slice(0, 3)

  return (
    <div className="market-home">
      <section className="market-hero">
        <div className="market-hero-copy">
          <p className="market-kicker"><span className="live-dot" /> THE NEXT BIG THING STARTS HERE</p>
          <h1>Find the work<br />you want to <em>be known for.</em></h1>
          <p className="market-hero-lede">
            Hackathons, competitions and experiences that take you further.
            Discover your next challenge, bring your people, and make something happen.
          </p>
          <div className="market-hero-actions">
            <Link to="/events" className="button-link button-link-primary">Explore opportunities <span>↗</span></Link>
            <Link to="/signup" className="button-link button-link-quiet">Create your account <span>→</span></Link>
          </div>
          <div className="market-hero-note"><span className="note-mark">✳</span><span>For the curious, the builders, and the almost-ready.</span></div>
        </div>
        <div className="market-hero-art" aria-label="Abstract editorial illustration of ideas coming together">
          <div className="hero-art-topline"><span>FIELD NOTES / 001</span><span>IDEAS INTO ACTION</span></div>
          <div className="hero-art-paper paper-one"><span>MAKE<br />IT REAL.</span><i>↗</i></div>
          <div className="hero-art-paper paper-two"><span className="paper-sun" /><span className="paper-lines" /></div>
          <div className="hero-art-disc"><span>YOUR NEXT<br />CHAPTER</span><b>✳</b></div>
          <div className="hero-art-stamp">OPEN<br />CALLS<br /><strong>→</strong></div>
          <div className="hero-art-bottom"><span>CURIOUS MINDS WELCOME</span><span>EMS® / DISCOVER</span></div>
        </div>
        <div className="market-hero-index"><span>01</span><span className="index-line" /><span>DISCOVER · COMPETE · GROW</span></div>
      </section>

      <section className="market-featured">
        <div className="market-section-head">
          <div><p className="market-kicker">YOUR NEXT OPPORTUNITY</p><h2>Good things are <em>happening.</em></h2></div>
          <Link to="/events" className="market-text-link">Browse all events <span>↗</span></Link>
        </div>
        {isLoading ? (
          <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3"><PassSkeleton /><PassSkeleton /><PassSkeleton /></div>
        ) : isError ? (
          <div className="market-empty"><span className="empty-symbol">↗</span><h3>We lost the signal.</h3><p>Try the discovery page in a moment.</p><Link to="/events">Open event discovery →</Link></div>
        ) : featured.length ? (
          <PassGrid events={featured} />
        ) : (
          <div className="market-empty"><span className="empty-symbol">✳</span><h3>Room for the next big idea.</h3><p>There aren't any published events yet. Check back soon, or explore the platform.</p><Link to="/events">Explore event discovery →</Link></div>
        )}
      </section>

      <section className="market-paths">
        <div className="market-section-head">
          <div><p className="market-kicker">A WAY IN FOR EVERY KIND OF AMBITION</p><h2>Choose your <em>next move.</em></h2></div>
          <p className="market-section-aside">Not sure where to start? Start with what excites you.</p>
        </div>
        <div className="path-grid">
          {paths.map((path) => (
            <Link key={path.number} to={`/events?type=${path.type}`} className="path-card">
              <span className="path-number">{path.number} / OPPORTUNITY</span>
              <span className="path-arrow">↗</span>
              <h3>{path.title}</h3><p>{path.detail}</p>
              <span className="path-bottom">FIND YOUR NEXT MOVE <b>→</b></span>
            </Link>
          ))}
        </div>
      </section>

      <section className="market-steps">
        <div className="steps-intro"><p className="market-kicker">LESS SCROLLING. MORE DOING.</p><h2>From “what if?”<br />to <em>“I'm in.”</em></h2><p>Everything you need to go from finding an opportunity to showing up ready.</p><Link to="/events" className="button-link button-link-light">Find an event <span>↗</span></Link></div>
        <div className="steps-list">
          <article><span>01</span><div><h3>Find your challenge</h3><p>Search by event type, deadline, or what you want to learn next.</p></div><b>↗</b></article>
          <article><span>02</span><div><h3>Go solo or build a team</h3><p>Join an individual event, create a team, or request to join one.</p></div><b>↗</b></article>
          <article><span>03</span><div><h3>Register and keep moving</h3><p>Complete registration, manage payment, and keep your passes in one place.</p></div><b>↗</b></article>
        </div>
      </section>

      <footer className="market-footer"><Link to="/" className="market-footer-brand">ems<span>®</span></Link><p>Find your people. Make your mark.</p><div><Link to="/events">Discover</Link><Link to="/teams">Teams</Link><Link to="/entries">My passes</Link></div><span className="footer-edition">BUILT FOR WHAT'S NEXT · 2026</span></footer>
    </div>
  )
}
