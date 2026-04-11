import { rmSync, existsSync } from 'node:fs'
import { resolve } from 'node:path'
import { globSync } from 'node:fs'

const root = resolve(process.cwd(), 'src')
const patterns = ['**/*.js']

const targets = patterns.flatMap((pattern) => globSync(pattern, { cwd: root, absolute: true }))

if (targets.length === 0) {
  console.log('No legacy JS artifacts found under src.')
  process.exit(0)
}

for (const target of targets) {
  if (existsSync(target)) {
    rmSync(target, { force: true })
    console.log(`Removed ${target}`)
  }
}

console.log(`Removed ${targets.length} legacy JS artifacts.`)
