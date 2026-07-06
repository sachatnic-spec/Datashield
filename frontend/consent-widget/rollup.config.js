import resolve from '@rollup/plugin-node-resolve';
import typescript from '@rollup/plugin-typescript';
import terser from '@rollup/plugin-terser';

export default {
  input: 'src/consent-widget.ts',
  output: [
    {
      exports: 'named',
      file: 'dist/consent-widget.js',
      format: 'umd',
      name: 'DataShieldConsentWidget',
      sourcemap: true
    },
    {
      file: 'dist/consent-widget.esm.js',
      format: 'esm',
      sourcemap: true
    }
  ],
  plugins: [
    resolve(),
    typescript({ tsconfig: './tsconfig.json', declaration: true, declarationDir: './dist' }),
    terser()
  ]
};
