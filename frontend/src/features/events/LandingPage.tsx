import { listVisibleEvents } from '@/api/events'
import { PassGrid } from '@/components/pass/PassGrid'
import { PassSkeleton } from '@/components/pass/PassSkeleton'
import { Button } from '@/components/ui/Button'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'

export function LandingPage() {
  const { data, isLoading, isError } = useQuery({
    queryKey: ['events', 'visible'],
    queryFn: listVisibleEvents,
  })
  const events = data ?? []
  const preview = events.slice(0, 3)

  return (
    <div className="landing-page">
      <section className="landing-hero">
        <div className="hero-copy">
          <div className="eyebrow"><span className="eyebrow-dot" /> YOUR PEOPLE ARE OUT THERE</div>
          <h1>Go where<br />you <em>feel</em> alive<span className="hero-period">.</span></h1>
          <p className="hero-description">A little curiosity goes a long way. Find gatherings, meet your kind of people, and make a story worth telling.</p>
          <div className="hero-actions">
            <Link to="/events"><Button className="hero-primary">Find your next thing <span aria-hidden="true">↗</span></Button></Link>
            <Link to="/signup" className="hero-secondary">It starts with you <span aria-hidden="true">→</span></Link>
          </div>
          <div className="hero-proof">
            <div className="avatar-stack" aria-hidden="true"><span>AK</span><span>MR</span><span>JP</span><span>+</span></div>
            <p><strong>People over plans.</strong><br />Always a good place to start.</p>
          </div>
        </div>
        <div className="hero-art" aria-label="Editorial collage representing ideas and events">
          <div className="art-frame frame-back"></div>
          <div className="art-photo">
            <div className="photo-sun"></div><div className="photo-horizon"></div>
            <div className="photo-person person-one"></div><div className="photo-person person-two"></div>
            <div className="photo-person person-three"></div><div className="photo-ground"></div>
            <div className="photo-grain"></div>
          </div>
          <div className="art-label label-top"><span className="label-star">✳</span><span>GOOD ENERGY<br /><b>ONLY.</b></span></div>
          <div className="art-sticker"><span>MAKE<br />MEMORIES</span><b>↗</b></div>
          <div className="art-caption"><span>FIG. 01</span><span>THE ART OF SHOWING UP</span></div>
          <span className="art-coordinate">17° 23' N — 78° 29' E</span>
          <div className="art-ring"></div>
        </div>
        <div className="hero-bottomline"><span>MEET</span><span className="bottomline-rule" /><span>MAKE</span><span className="bottomline-rule" /><span>REMEMBER</span><span className="bottomline-tail">A LIFE LESS SCROLLED ↓</span></div>
      </section>

      <section className="discovery-section" id="events">
        <div className="section-heading">
          <div><p className="section-kicker">OUT THERE, HAPPENING</p><h2>Plans worth <em>making.</em></h2></div>
          <Link to="/events" className="text-link">Explore everything <span aria-hidden="true">↗</span></Link>
        </div>
        {isLoading ? (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3"><PassSkeleton /><PassSkeleton /><PassSkeleton /></div>
        ) : isError ? (
          <div className="empty-state"><span className="empty-mark">↗</span><p>Something interrupted the connection.</p><Link to="/events">Try the events page →</Link></div>
        ) : preview.length ? (
          <PassGrid events={preview} />
        ) : (
          <div className="empty-state"><span className="empty-mark">✳</span><p>Something good is on its way.</p><span>There are no published events right now. Come back soon.</span></div>
        )}
      </section>

      <section className="manifesto">
        <div className="manifesto-mark">EMS<span>®</span></div>
        <p>The best stories rarely start at home.<br /><em>Go make one.</em></p>
        <Link to="/events" aria-label="Explore all events" className="manifesto-arrow">↗</Link>
      </section>

      <section className="how-section">
        <div className="section-heading">
          <div><p className="section-kicker">NO GATEKEEPING. NO GUESSWORK.</p><h2>From curious to <em>count me in.</em></h2></div>
        </div>
        <div className="how-grid">
          <article className="how-card"><span className="how-number">01 / FIND</span><div className="how-icon icon-sun">✳</div><h3>Follow the feeling.</h3><p>Explore events by what sparks your interest. Your next favorite thing is closer than you think.</p><Link to="/events">See what's out there <span>↗</span></Link></article>
          <article className="how-card"><span className="how-number">02 / CONNECT</span><div className="how-icon icon-orbit">◎</div><h3>Bring your people.</h3><p>Go solo or build a team. Meet people who get excited about the same things you do.</p><Link to="/teams">Find your crew <span>↗</span></Link></article>
          <article className="how-card"><span className="how-number">03 / SHOW UP</span><div className="how-icon icon-ticket">▤</div><h3>Make it official.</h3><p>Register, secure your pass, and leave the logistics to us. The good part is what happens next.</p><Link to="/signup">Get started <span>↗</span></Link></article>
        </div>
      </section>

      <footer className="landing-footer">
        <Link to="/" className="footer-brand">ems<span>®</span></Link>
        <p>Good people. Good plans. Better stories.</p>
        <div><Link to="/events">Events</Link><Link to="/login">Log in</Link><Link to="/signup">Join EMS</Link></div>
      </footer>
    </div>
  )
}
