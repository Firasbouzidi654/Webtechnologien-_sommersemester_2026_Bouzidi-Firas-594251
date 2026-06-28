export const CLASSROOM_THEMES = [
  {
    name: 'Animals & Nature',
    focuses: [
      'Forest Adventure',
      'Butterfly Life Cycle',
      'Farm Animal Sounds',
      'Bird Watching',
      'Ocean Creatures',
      'Insect Discovery'
    ]
  },
  {
    name: 'Space Explorers',
    focuses: [
      'The Solar System',
      'Moon Missions',
      'Build a Rocket',
      'Astronaut Training',
      'Planet Earth',
      'Star Patterns'
    ]
  },
  {
    name: 'Healthy Habits',
    focuses: [
      'Rainbow Plate',
      'Handwashing Lab',
      'Rest and Recharge',
      'Strong Teeth',
      'Body Signals',
      'Kind Choices'
    ]
  },
  {
    name: 'Seasons & Weather',
    focuses: [
      'Cloud Watch',
      'Rainy Day Science',
      'Autumn Leaves',
      'Winter Warmth',
      'Spring Garden',
      'Sunny Shadows'
    ]
  },
  {
    name: 'Around the World',
    focuses: [
      'Map Makers',
      'Home Designs',
      'Greeting Songs',
      'Festival Colors',
      'Market Day',
      'Landmark Builders'
    ]
  },
  {
    name: 'Colors & Creativity',
    focuses: [
      'Color Mixing',
      'Shape Collage',
      'Pattern Painting',
      'Texture Hunt',
      'Clay Creations',
      'Storybook Art'
    ]
  },
  {
    name: 'Music & Movement',
    focuses: [
      'Rhythm Parade',
      'Freeze Dance',
      'Instrument Discovery',
      'Fast and Slow',
      'Sound Patterns',
      'Action Stories'
    ]
  },
  {
    name: 'Community Helpers',
    focuses: [
      'Fire Safety',
      'Post Office Play',
      'Doctors and Nurses',
      'Construction Crew',
      'Library Visit',
      'Neighborhood Map'
    ]
  },
  {
    name: 'Dinosaurs',
    focuses: [
      'Fossil Dig',
      'Dinosaur Footprints',
      'Herbivore Cafe',
      'Volcano Experiment',
      'Prehistoric Habitats',
      'Dino Size Sorting'
    ]
  },
  {
    name: 'Under the Sea',
    focuses: [
      'Coral Reef',
      'Tide Pool Treasures',
      'Whale Songs',
      'Floating and Sinking',
      'Sea Turtle Journey',
      'Shell Patterns'
    ]
  }
];

function pickRandom(items) {
  return items[Math.floor(Math.random() * items.length)];
}

function normalizedWords(value) {
  return new Set(String(value).toLowerCase().split(/[^a-z0-9]+/).filter((word) => word.length > 3));
}

export function isDistinctFocus(themeName, focus) {
  if (String(themeName).trim().toLowerCase() === String(focus).trim().toLowerCase()) return false;

  const themeWords = normalizedWords(themeName);
  const focusWords = normalizedWords(focus);
  return ![...themeWords].some((word) => focusWords.has(word));
}

export function randomClassroomThemeFocus() {
  const theme = pickRandom(CLASSROOM_THEMES);
  const distinctFocuses = theme.focuses.filter((focus) => isDistinctFocus(theme.name, focus));

  return {
    theme: theme.name,
    focus: pickRandom(distinctFocuses.length > 0 ? distinctFocuses : theme.focuses)
  };
}
