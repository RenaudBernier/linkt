
import { Box, Container, Divider, Link, Stack, Typography } from '@mui/material';

const sections: Array<{ title: string; links: string[] }> = [
  { title: 'Product',       links: ['Features', 'Pricing', 'Integrations', 'Documentation'] },
  { title: 'Company',       links: ['About Us', 'Careers', 'News', 'Contact'] },
  { title: 'For Organizers',links: ['Create Events', 'Manage Tickets', 'Check-in App', 'Analytics'] },
  { title: 'For Students',  links: ['Browse Events', 'My Tickets', 'Calendar', 'Support'] },
  { title: 'Resources',     links: ['Help Center', 'Community Forum', 'System Status', 'Report an Issue'] },
];

export default function Footer() {
  return (
    <Box component="footer" sx={{ bgcolor: 'grey.100', color: 'text.primary' }}>
      <Container sx={{ py: { xs: 4, sm: 6 } }}>
        {/* Columns with CSS Grid */}
        <Box
          sx={{
            display: 'grid',
            gap: 4,
            gridTemplateColumns: {
              xs: '1fr',
              sm: 'repeat(2, 1fr)',
              md: 'repeat(3, 1fr)',
              lg: 'repeat(4, 1fr)',
              xl: 'repeat(5, 1fr)', 
            },
          }}
        >
          {sections.map((sec) => (
            <Box key={sec.title}>
              <Typography variant="subtitle1" fontWeight={700} gutterBottom>
                {sec.title}
              </Typography>
              <Stack spacing={1}>
                {sec.links.map((label) => (
                  <Link
                    key={label}
                    href="#"
                    underline="hover"
                    color="text.secondary"
                    sx={{ display: 'inline-block', lineHeight: 1.75, '&:hover': { color: 'text.primary' } }}
                  >
                    {label}
                  </Link>
                ))}
              </Stack>
            </Box>
          ))}
        </Box>

        <Divider sx={{ my: 4 }} />

        {/* Blurb */}
        <Stack spacing={1} sx={{ mb: 2 }}>
          <Typography variant="h6" fontWeight={700}>
            Linkt — Campus Events & Ticketing
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Discover and attend campus events, claim QR-coded tickets, and manage check-ins. Organizers can create events,
            track attendance, and view analytics. Admins oversee organizations and moderate content.
          </Typography>
        </Stack>

        {/* Bottom bar */}
        <Stack
          direction={{ xs: 'column', sm: 'row' }}
          spacing={2}
          alignItems="center"
          justifyContent="space-between"
        >
          <Typography variant="body2" color="text.secondary">
            © {new Date().getFullYear()} Linkt. All rights reserved.
          </Typography>
          <Stack direction="row" spacing={3}>
            <Link href="#" color="text.secondary" underline="hover">Terms</Link>
            <Link href="#" color="text.secondary" underline="hover">Privacy</Link>
            <Link href="#" color="text.secondary" underline="hover">Site Map</Link>
            <Link href="#" color="text.secondary" underline="hover">Accessibility</Link>
          </Stack>
        </Stack>
      </Container>
    </Box>
  );
}
